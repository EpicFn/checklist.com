package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 클럽 멤버의 유효성을 검사하는 서비스입니다.
 * 클럽 멤버의 역할을 확인하거나 클럽 멤버 여부를 확인하는 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ClubMemberValidService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final MemberService memberService;

    /**
     * 클럽 멤버의 역할을 확인합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @param roles 요청된 역할 배열
     * @return 요청된 역할 중 하나라도 일치하면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean checkMemberRole(Long clubId, Long memberId, ClubMemberRole[] roles) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "클럽 멤버가 존재하지 않습니다."));

        // 요청된 역할이 클럽 멤버의 역할 중 하나인지 확인
        for (ClubMemberRole role : roles) {
            if (clubMember.getRole() == role) {
                return true; // 역할이 일치하면 true 반환
            }
        }
        return false; // 일치하는 역할이 없으면 false 반환
    }

    /**
     * 클럽 멤버인지 확인합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @return 클럽 멤버 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubMember(Long clubId, Long memberId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        return clubMemberRepository.existsByClubAndMember(club, member);
    }

}
