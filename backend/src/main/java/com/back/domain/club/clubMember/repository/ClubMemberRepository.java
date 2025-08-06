package com.back.domain.club.clubMember.repository;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findAllByClubId(Long clubId);

    // 요청 이메일 목록에 해당하는 ClubMember 정보를 한 번에 조회
    @Query("""
       SELECT cm
       FROM ClubMember cm
       WHERE cm.club.id = :clubId
              AND cm.member.memberInfo.email
              IN :emails
    """)
    List<ClubMember> findClubMembersByClubIdAndEmails(@Param("clubId") Long clubId, @Param("emails") List<String> emails);

    //정원 체크를 위한 현재 활동 멤버 수 조회 (탈퇴 제외)
    @Query("""
    SELECT COUNT(cm)
    FROM ClubMember cm
    WHERE cm.club.id = :clubId
        AND cm.state != 'WITHDRAWN'
    """)
    long countActiveMembersByClubId(@Param("clubId") Long clubId);


    Optional<ClubMember> findByClubAndMember(Club club, Member member);

    // 특정 모임에 특정 멤버가 특정 상태로 존재하는지 확인
    Optional<ClubMember> findByClubAndMemberAndState(Club club, Member member, ClubMemberState clubMemberState);

    List<ClubMember> findByClubAndState(Club club, ClubMemberState clubMemberState);

    List<ClubMember> findByClub(Club club);

    boolean existsByClubAndMember(Club club, Member member);

    // 특정 모임에 특정 멤버가 특정 상태로 존재하는지 확인
    boolean existsByClubAndMemberAndState(Club club, Member member, ClubMemberState clubMemberState);

    boolean existsByClubAndMemberAndRoleIn(Club club, Member member, List<ClubMemberRole> roles);

    List<ClubMember> findAllByMember(Member user);
}
