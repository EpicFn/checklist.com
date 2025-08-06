package com.back.domain.member.friend.service;

import com.back.domain.member.friend.dto.FriendDto;
import com.back.domain.member.friend.dto.FriendMemberDto;
import com.back.domain.member.friend.dto.FriendStatusDto;
import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.friend.error.FriendErrorCode;
import com.back.domain.member.friend.repository.FriendRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.error.MemberErrorCode;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final FriendRepository friendRepository;

    /**
     * 내 친구 목록을 조회하는 메서드
     * @param memberId 로그인 회원 아이디
     * @return List<FriendsResDto>
     */
    public List<FriendDto> getFriends(Long memberId, FriendStatusDto statusFilter) {
        // 로그인 회원
        Member member = memberRepository.findWithFriendsById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));

        Stream<Friend> allFriends = Stream.concat(
                member.getFriendshipsAsMember1().stream(), // member1로 등록된 친구 관계
                member.getFriendshipsAsMember2().stream()  // member2로 등록된 친구 관계
        );

        Stream<Friend> filteredFriendsStream;
        if (statusFilter == null) {
            filteredFriendsStream = allFriends;
        } else {
            filteredFriendsStream = switch (statusFilter) {
                // 친구
                case ACCEPTED -> allFriends
                        .filter(friend ->
                                friend.getStatus() == FriendStatus.ACCEPTED
                        );
                // 내가 보낸 요청
                case SENT -> allFriends
                        .filter(friend ->
                                friend.getStatus() == FriendStatus.PENDING && friend.getRequestedBy().getId().equals(memberId)
                        );
                // 내가 받은 요청
                case RECEIVED -> allFriends
                        .filter(friend ->
                                friend.getStatus() == FriendStatus.PENDING && !friend.getRequestedBy().getId().equals(memberId)
                        );
                default -> Stream.empty();
            };
        }

        // 친구 목록 조회
        return filteredFriendsStream
                .map(friend -> new FriendDto(friend, friend.getOther(member))) // DTO 변환
                .sorted(Comparator.comparing(FriendDto::friendNickname))             // 이름 오름차순
                .collect(Collectors.toList());
    }

    /**
     * 친구 엔티티를 아이디로 조회하는 메서드
     * @param friendId
     * @return Friend
     */
    public Friend getFriendById(Long friendId) {
        return friendRepository
                .findById(friendId)
                .orElseThrow(() -> new NoSuchElementException(FriendErrorCode.FRIEND_NOT_FOUND.getMessage()));
    }

    /**
     * 친구 추가 요청을 처리하는 메서드
     * @param memberId    로그인 회원 아이디
     * @param friendEmail 친구(Member) 이메일
     * @return FriendDto
     */
    @Transactional
    public FriendDto addFriend(Long memberId, String friendEmail) {
        // 로그인 회원(친구 요청을 보낸 회원)
        Member requester = getMember(memberId);

        // 친구 요청을 받는 회원
        MemberInfo responderInfo = getFriendMemberInfoByEmail(friendEmail);
        Member responder = responderInfo.getMember();

        // 자기 자신을 친구로 추가하는 경우 예외 처리
        if (requester.equals(responder)) {
            throw new ServiceException(FriendErrorCode.FRIEND_REQUEST_SELF);
        }

        // id 순
        Member lowerMember = memberId < responder.getId() ? requester : responder;
        Member higherMember = memberId < responder.getId() ? responder : requester;

        // 이미 친구인 경우 예외 처리
        friendRepository
                .findByMembers(requester, responder)
                .ifPresent(existingFriend -> {
                    ErrorCode errorCode;
                    // 친구 관계 상태에 따라 에러 메시지
                    switch (existingFriend.getStatus()) {
                        case PENDING -> {
                            // 요청자 여부에 따라 에러 메시지
                            if (existingFriend.getRequestedBy().equals(requester)) {
                                errorCode = FriendErrorCode.FRIEND_ALREADY_REQUEST_PENDING;
                            } else {
                                errorCode = FriendErrorCode.FRIEND_ALREADY_RESPOND_PENDING;
                            }
                        }
                        case ACCEPTED -> errorCode = FriendErrorCode.FRIEND_ALREADY_ACCEPTED;
                        case REJECTED -> errorCode = FriendErrorCode.FRIEND_ALREADY_REJECTED;
                        default -> errorCode = FriendErrorCode.FRIEND_STATUS_UNHANDLED;
                    }
                    throw new ServiceException(errorCode);
                });

        // 친구 요청 생성
        Friend friend = Friend.builder()
                .requestedBy(requester)
                .member1(lowerMember)
                .member2(higherMember)
                .status(FriendStatus.PENDING)
                .build();

        // 친구 요청 저장
        friendRepository.save(friend);

        return new FriendDto(friend, responder);
    }

    /**
     * 친구 요청을 수락하는 메서드
     * @param memberId 로그인 회원 아이디
     * @param friendId 친구 엔티티 아이디
     * @return FriendDto
     */
    @Transactional
    public FriendDto acceptFriend(Long memberId, Long friendId) {
        // 로그인 회원(친구 요청을 받은 회원)
        Member me = getMember(memberId);

        // 친구 엔티티
        Friend friend = getFriendById(friendId);
        // 친구 유효성 검사
        validateFriend(friend, me);

        // 친구 요청을 보낸 회원
        Member friendMember = friend.getOther(me);
        // 친구 회원 유효성 검사

        // 받는이가 아닌 요청자가 친구 요청을 수락하는 경우 예외 처리
        if (me.equals(friend.getRequestedBy())) {
            throw new ServiceException(FriendErrorCode.FRIEND_REQUEST_NOT_ALLOWED_ACCEPT);
        }

        // 친구 요청의 상태가 PENDING이 아닌 경우 예외 처리
        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new ServiceException(FriendErrorCode.FRIEND_ALREADY_ACCEPTED);
        }

        // 친구 요청 수락
        friend.setStatus(FriendStatus.ACCEPTED);

        return new FriendDto(friend, friendMember);
    }

    /**
     * 친구 요청을 거절하는 메서드
     * @param memberId 로그인 회원 아이디
     * @param friendId 친구 엔티티 아이디
     * @return FriendDto
     */
    @Transactional
    public FriendDto rejectFriend(Long memberId, Long friendId) {
        // 로그인 회원(친구 요청을 받은 회원)
        Member me = getMember(memberId);

        // 친구 엔티티
        Friend friend = getFriendById(friendId);
        // 친구 유효성 검사
        validateFriend(friend, me);

        // 받는이가 아닌 요청자가 친구 요청을 거절하는 경우 예외 처리
        if (me.equals(friend.getRequestedBy())) {
            throw new ServiceException(FriendErrorCode.FRIEND_REQUEST_NOT_ALLOWED_REJECT);
        }

        // 이미 친구인 경우 예외 처리
        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new ServiceException(FriendErrorCode.FRIEND_ALREADY_ACCEPTED_NOT_ALLOWED);
        }

        // 친구 요청 거절
        friend.setStatus(FriendStatus.REJECTED);

        // 친구 요청을 보낸 회원
        Member friendMember = friend.getOther(me);
        return new FriendDto(friend, friendMember);
    }

    /**
     * 친구 삭제를 처리하는 메서드
     * @param memberId 로그인 회원 아이디
     * @param friendId 친구 엔티티 아이디
     * @return FriendDelDto
     */
    @Transactional
    public FriendMemberDto deleteFriend(Long memberId, Long friendId) {
        // 로그인 회원
        Member me = getMember(memberId);

        // 친구 엔티티
        Friend friend = getFriendById(friendId);
        // 친구 유효성 검사
        validateFriend(friend, me);

        // 친구 요청의 상태가 ACCEPTED가 아닌 경우 예외 처리
        if (friend.getStatus() != FriendStatus.ACCEPTED) {
            throw new ServiceException(FriendErrorCode.FRIEND_NOT_ACCEPTED);
        }
        // 삭제하는 친구
        Member friendMember = friend.getOther(me);

        // 친구 삭제
        friendRepository.delete(friend);

        return new FriendMemberDto(friendMember);
    }

    /**
     * 회원 정보를 가져오는 메서드
     * @param memberId 로그인 회원 아이디
     * @return Member
     */
    private Member getMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    /**
     * 친구 회원 정보를 이메일로 가져오는 메서드
     * @param friendEmail
     * @return MemberInfo
     */
    private MemberInfo getFriendMemberInfoByEmail(String friendEmail) {
        return memberInfoRepository
                .findByEmailWithMember(friendEmail)
                .orElseThrow(() -> new NoSuchElementException(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    /**
     * 사용자의 친구 관계인지 확인
     * @param friend 친구 엔티티
     * @param me 로그인 회원
     */
    private void validateFriend(Friend friend, Member me) {
        if (!friend.involves(me)) {
            throw new ServiceException(FriendErrorCode.FRIEND_ACCESS_DENIED);
        }
    }
}
