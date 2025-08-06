package com.back.domain.club.clubLink.service;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enums.ClubApplyResult;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubLinkService {
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubLinkRepository clubLinkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ClubLinkDtos.CreateClubLinkResponse createClubLink(Member user, Long clubId) {
        Club club = isClubExist(clubId);

        //권한 체크하여 Host, Manager이 아닐 시 에러
        validateClubManagerOrHost(club, user);

        LocalDateTime now = LocalDateTime.now();

        //기존 활성 링크가 있을 시 해당 링크 반환
        Optional<ClubLink> existingLink = clubLinkRepository.findByClubAndExpiresAtAfter(club, now);
        if (existingLink.isPresent()) {
            String existingCode = existingLink.get().getInviteCode();
            // 기존 링크가 존재하면, 완전한 URL 형태로 반환
            String link = "http://localhost:3000/clubs/invite?token=" + existingCode;
            return new ClubLinkDtos.CreateClubLinkResponse(link);
        }

        //UUID 기반 초대 코드 생성
        String inviteCode = UUID.randomUUID().toString();

        LocalDateTime expireAt = now.plusDays(7);

        //클럽 링크 객체 생성 및 db 저장
        ClubLink clubLink = ClubLink.builder()
                .inviteCode(inviteCode)
                .createdAt(now)
                .expiresAt(expireAt)
                .club(club)
                .build();

        clubLinkRepository.save(clubLink);

        String link = "http://localhost:3000/clubs/invite?token=" + inviteCode;
        return new ClubLinkDtos.CreateClubLinkResponse(link);
    }

    public ClubLinkDtos.CreateClubLinkResponse getExistingClubLink(Member user, @Positive Long clubId) {
        Club club = isClubExist(clubId);

        //권한 체크하여 Host, Manager이 아닐 시 에러
        validateClubManagerOrHost(club, user);

        LocalDateTime now = LocalDateTime.now();

        ClubLink existingLink = clubLinkRepository.findByClubAndExpiresAtAfter(club, now)
                .orElseThrow(() -> new ServiceException(400, "활성화된 초대 링크를 찾을 수 없습니다."));

        // 기존 링크를 완전한 URL 형태로 반환
        String link = "http://localhost:3000/clubs/invite?token=" + existingLink.getInviteCode();
        return new ClubLinkDtos.CreateClubLinkResponse(link);
    }

    public ClubApplyResult applyToPrivateClub(Member user, String token) {
        // 토큰 유효성 체크
        ClubLink clubLink = validateInviteTokenOrThrow(token);
        Club club = clubLink.getClub();

        Optional<ClubMember> existingMemberOtp = clubMemberRepository.findByClubAndMember(club, user);
        if (existingMemberOtp.isPresent()) {
            ClubMember existingMember = existingMemberOtp.get();
            return switch (existingMember.getState()) {
                case JOINING -> ClubApplyResult.ALREADY_JOINED;
                case APPLYING -> ClubApplyResult.ALREADY_APPLYING;
                case INVITED -> ClubApplyResult.ALREADY_INVITED;
                default -> throw new ServiceException(400, "해당 상태에서는 가입할 수 없습니다.");
            };
        }

        ClubMember clubMember = ClubMember.builder()
                .member(user)
                .role(ClubMemberRole.PARTICIPANT)
                .state(ClubMemberState.APPLYING)
                .club(club)
                .build();

        clubMemberRepository.save(clubMember);

        return ClubApplyResult.SUCCESS;
    }

    public ClubControllerDtos.SimpleClubInfoResponse getClubInfoByInvitationToken(String token) {
        ClubLink clubLink = validateInviteTokenOrThrow(token);
        Club club = clubLink.getClub();

        Member leader = memberRepository.findById(club.getLeaderId())
                .orElseThrow(() -> new ServiceException(400, "해당 아이디의 모임장을 찾을 수 없습니다."));

        return new ClubControllerDtos.SimpleClubInfoResponse(
                club.getId(),
                club.getName(),
                club.getCategory().name(),
                club.getImageUrl(),
                club.getMainSpot(),
                club.getEventType().name(),
                club.getStartDate().toString(),
                club.getEndDate().toString(),
                club.getLeaderId(),
                leader.getNickname()
        );
    }

    //===============================기타 메서드================================

    public Club isClubExist(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 클럽을 찾을 수 없습니다."));
    }

    public void validateClubManagerOrHost(Club club, Member user) {
        if (!clubMemberRepository.existsByClubAndMemberAndRoleIn(
                club,
                user,
                List.of(ClubMemberRole.MANAGER, ClubMemberRole.HOST))) {
            throw new ServiceException(400, "호스트나 매니저만 초대 링크를 관리할 수 있습니다.");
        }
    }

    // 토큰 유효성 검사, 유효하면 ClubLink 반환, 아니면 예외 발생
    public ClubLink validateInviteTokenOrThrow(String token) {
        ClubLink clubLink = clubLinkRepository.findByInviteCode(token)
                .orElseThrow(() -> new ServiceException(400, "초대 토큰이 유효하지 않습니다."));
        if (clubLink.isExpired()) {
            throw new ServiceException(400, "초대 토큰이 만료되었습니다.");
        }
        return clubLink;
    }
}
