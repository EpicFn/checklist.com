package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.dtos.ClubMemberDtos;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;
    private final MemberService memberService;
    private final ClubMemberValidService clubMemberValidService;
    private final Rq rq;



    /**
     * 클럽에 멤버를 추가합니다. (테스트용, controller에선 사용하지 않음)
     * @param clubId 클럽 ID
     * @param member 추가할 멤버
     * @param role 클럽 멤버 역할
     */
    @Transactional
    public ClubMember addMemberToClub(Long clubId, Member member, ClubMemberRole role) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        ClubMember clubMember = ClubMember.builder()
                .member(member)
                .role(role) // 기본 역할은 MEMBER
                .state(ClubMemberState.INVITED) // 기본 상태는 INVITED
                .build();

        club.addClubMember(clubMember);

        return clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽에 멤버를 추가합니다. 요청된 이메일을 기반으로 중복된 멤버는 제외하고 추가합니다.
     * @param clubId 클럽 ID
     * @param reqBody 클럽 멤버 등록 요청 DTO
     */
    @Transactional
    public void addMembersToClub(Long clubId, ClubMemberDtos.ClubMemberRegisterRequest reqBody) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 1. 요청 데이터에서 이메일 기준 중복 제거 (나중에 들어온 정보가 우선)
        Map<String, ClubMemberDtos.ClubMemberRegisterInfo> uniqueMemberInfoByEmail = reqBody.members().stream()
                .collect(Collectors.toMap(
                        ClubMemberDtos.ClubMemberRegisterInfo::email,
                        info -> info,
                        (existing, replacement) -> replacement // 키가 중복될 경우, 기존 값(existing)을 새로운 값(replacement)으로 덮어씀
                ));

        // 2. 요청된 이메일 목록을 한 번에 조회하여 Map으로 변환 (효율적인 탐색을 위해)
        List<String> requestEmails = new ArrayList<>(uniqueMemberInfoByEmail.keySet());
        Map<String, ClubMember> existingMembersByEmail = clubMemberRepository.findClubMembersByClubIdAndEmails(clubId, requestEmails)
                .stream()
                .collect(Collectors.toMap(cm -> cm.getMember().getMemberInfo().getEmail(), cm -> cm));

        // 3. 신규 추가/상태 변경할 멤버 목록 준비
        List<ClubMember> membersToSave = new ArrayList<>();
        List<ClubMemberDtos.ClubMemberRegisterInfo> newMemberRequests = new ArrayList<>();

        uniqueMemberInfoByEmail.values().forEach(memberInfo -> {
            ClubMember existingMember = existingMembersByEmail.get(memberInfo.email());

            if (existingMember != null) {
                if (existingMember.getState() == ClubMemberState.WITHDRAWN) {
                    existingMember.updateState(ClubMemberState.INVITED);
                    // 요청된 역할로 업데이트
                    existingMember.updateRole(ClubMemberRole.fromString(memberInfo.role().toUpperCase()));
                    membersToSave.add(existingMember);
                }
            } else {
                newMemberRequests.add(memberInfo);
            }
        });

        // 4. 정원 초과 여부 검사 (효율적인 COUNT 쿼리 사용)
        long currentActiveMembers = clubMemberRepository.countActiveMembersByClubId(clubId);
        if (currentActiveMembers + newMemberRequests.size() > club.getMaximumCapacity()) {
            throw new ServiceException(400, "클럽의 최대 멤버 수를 초과했습니다.");
        }

        // 5. 새로운 멤버 엔티티 생성
        for (ClubMemberDtos.ClubMemberRegisterInfo memberInfo : newMemberRequests) {
            Member member = memberService.findMemberByEmail(memberInfo.email());
            ClubMember newClubMember = ClubMember.builder()
                    .member(member)
                    .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase()))
                    .state(ClubMemberState.INVITED)
                    .build();
            club.addClubMember(newClubMember); // 양방향 연관관계 설정
            membersToSave.add(newClubMember);
        }

        // 6. 변경/추가된 모든 멤버 정보를 한 번에 저장 (Batch Insert/Update)
        if (!membersToSave.isEmpty()) {
            clubMemberRepository.saveAll(membersToSave);
        }
    }

    /**
     * 클럽에서 멤버를 탈퇴시킵니다.
     * @param clubId 클럽 ID
     * @param memberId 탈퇴할 멤버 ID
     */
    @Transactional
    public void withdrawMemberFromClub(Long clubId, Long memberId) {
        Member user = rq.getActor();
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "클럽 멤버가 존재하지 않습니다."));

        // 호스트 본인이 탈퇴하려는 경우 예외 처리
        if (user.getId().equals(memberId) && clubMember.getRole() == ClubMemberRole.HOST) {
            throw new ServiceException(400, "호스트는 탈퇴할 수 없습니다.");
        }

        // 클럽에서 멤버 탈퇴
        clubMember.updateState(ClubMemberState.WITHDRAWN);
        clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽 멤버의 역할을 변경합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @param role 변경할 역할
     */
    @Transactional
    public void changeMemberRole(Long clubId, Long memberId, @NotBlank String role) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 호스트 본인이 역할을 변경하려는 경우 예외 처리
        if (member.getId().equals(rq.getActor().getId())) {
            throw new ServiceException(400, "호스트는 본인의 역할을 변경할 수 없습니다.");
        }

        // 호스트 권한 부여 금지
        if (role.equalsIgnoreCase(ClubMemberRole.HOST.name())) {
            throw new ServiceException(400, "호스트 권한은 직접 부여할 수 없습니다.");
        }

        // 역할 변경
        clubMember.updateRole(ClubMemberRole.fromString(role.toUpperCase()));
        clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽의 멤버 목록을 조회합니다.
     * @param clubId 클럽 ID
     * @param state 상태 필터링 (선택적)
     * @return 클럽 멤버 목록 DTO
     */
    @Transactional(readOnly = true)
    public ClubMemberDtos.ClubMemberResponse getClubMembers(Long clubId, String state) {
        // 클럽 확인
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 클럽멤버 목록 반환
        List<ClubMember> clubMembers;
        if(state != null){
            clubMembers = clubMemberRepository.findByClubAndState(club, ClubMemberState.fromString(state));
        }
        else {
            clubMembers = clubMemberRepository.findByClub(club);
        }

        // 클럽 멤버 정보를 DTO로 변환
        List<ClubMemberDtos.ClubMemberInfo> memberInfos = clubMembers.stream()
                .filter(clubMember -> clubMember.getMember() != null) // 멤버가 존재하는 경우만 필터링
                .filter(clubMember -> clubMember.getState() != ClubMemberState.WITHDRAWN) // 탈퇴한 멤버 제외
                .map(clubMember -> {
                    Member m = clubMember.getMember();

                    return new ClubMemberDtos.ClubMemberInfo(
                            clubMember.getId(),
                            m.getId(),
                            m.getNickname(),
                            m.getTag(),
                            clubMember.getRole(),
                            Optional.ofNullable(m.getMemberInfo())
                                    .map(MemberInfo::getEmail)
                                    .orElse(""),
                            m.getMemberType(),
                            Optional.ofNullable(m.getMemberInfo())
                                    .map(MemberInfo::getProfileImageUrl)
                                    .orElse(""),
                            clubMember.getState()
                    );
                }).toList();

        return new ClubMemberDtos.ClubMemberResponse(memberInfos);

    }

    /**
     * 클럽과 멤버로 가입 완료한 클럽 멤버 조회
     * @param club 모임 엔티티
     * @param member 멤버 엔티티
     * @return 클럽 멤버 엔티티
     */
    public ClubMember getClubMember(Club club, Member member) {
        return clubMemberRepository.findByClubAndMemberAndState(club, member, ClubMemberState.JOINING)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));
    }

    /**
     * 클럽과 멤버로 클럽 멤버 존재 여부 확인
     * @param club 모임 엔티티
     * @param member 멤버 엔티티
     * @return 클럽 멤버 존재 여부
     */
    public boolean existsByClubAndMember(Club club, Member member) {
        return clubMemberRepository
                .existsByClubAndMemberAndState(club, member, ClubMemberState.JOINING);
    }

    /**
     * 클럽 가입 신청을 승인하거나 거절합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @param approve true면 승인, false면 거절
     */
    @Transactional
    public void handleMemberApplication(Long clubId, Long memberId, boolean approve) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(400, "가입 신청 상태가 아닙니다."));

        // 현재 상태가 APPLYING이 아닌 경우 예외 처리
        if (clubMember.getState() != ClubMemberState.APPLYING) {
            if(clubMember.getState() == ClubMemberState.JOINING)
                throw new ServiceException(400, "이미 가입 상태입니다.");
            else
                throw new ServiceException(400, "가입 신청 상태가 아닙니다.");
        }

        // 승인 또는 거절 처리
        if (approve) {
            clubMember.updateState(ClubMemberState.JOINING);
            clubMemberRepository.save(clubMember);
        } else {
            clubMemberRepository.delete(clubMember);
            // 거절 시 클럽에서 멤버 제거
            club.removeClubMember(clubMember);
        }


    }
}
