package com.back.domain.club.club.checker;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component("clubAuthorizationChecker")
@RequiredArgsConstructor
public class ClubAuthorizationChecker {
    private final MemberService memberService;
    private final ClubRepository clubRepository;
    private final ClubMemberService clubMemberService;
    private final ClubService clubService;

    /**
     * 모임이 존재하는지 확인
     * @param clubId 모임 ID
     * @return 모임 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubExists(Long clubId) {
        return clubRepository.existsById(clubId);
    }

    /**
     * 모임 호스트 권한이 있는지 확인
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubHost(Long clubId, Long memberId) {
        Club club = clubService.getClub(clubId);

        return Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 모임 호스트 권한이 있는지 확인 (종료 안된 활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubHost(Long clubId, Long memberId) {
        Club club = clubService.getValidAndActiveClub(clubId);

        return Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 모임 매니저의 역할 확인 (종료 안된 활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 매니저 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubManager(Long clubId, Long memberId) {
        Club club = clubService.getValidAndActiveClub(clubId);
        Member member = memberService.getMember(memberId);
        ClubMember clubMember = clubMemberService.getClubMember(club, member);

        return clubMember.getRole() == ClubMemberRole.MANAGER;
    }

    /**
     * 모임 호스트 또는 매니저 권한 확인 (종료 안된 활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 또는 매니저 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubManagerOrHost(Long clubId, Long memberId) {
        Club club = clubService.getValidAndActiveClub(clubId);
        Member member = memberService.getMember(memberId);
        ClubMember clubMember = clubMemberService.getClubMember(club, member);

        return clubMember.getRole() == ClubMemberRole.MANAGER
                || Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 가입된(JOINING) 모임 참여자 여부 확인 (활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 참여자 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubMember(Long clubId, Long memberId) {
        Club club = clubService.getActiveClub(clubId);
        Member member = memberService.getMember(memberId);
        return clubMemberService.existsByClubAndMember(club, member);
    }

    /**
     * 로그인 유저가 요청한 멤버 ID와 일치하는지 확인
     * @param targetMemberId 멤버 ID
     * @param currentUserId 로그인 유저 ID
     * @return 로그인 유저 - 요청한 멤버 ID 일치 여부
     */
    @Transactional(readOnly = true)
    public boolean isSelf(Long targetMemberId, Long currentUserId) {
        return Objects.equals(targetMemberId, currentUserId);
    }
}
