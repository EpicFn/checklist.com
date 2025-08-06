package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.dtos.MyClubControllerDtos;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyClubService {
    private final ClubService clubService;
    private final ClubMemberRepository clubMemberRepository;
    private final MemberService memberService;
    private final Rq rq;

    /**
     * 클럽 초대를 수락하거나 거절하는 메서드
     * @param clubId 클럽 ID
     * @param accept 초대 수락 여부 (true면 수락, false면 거절)
     * @return 클럽 정보
     */
    @Transactional
    public Club handleClubInvitation(Long clubId, boolean accept) {
        // 멤버 가져오기
        Member user = rq.getActor();
        // 클럽 ID로 클럽 가져오기
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, user)
                .orElseThrow(() -> new ServiceException(400, "클럽 초대 상태가 아닙니다."));

        // 클럽 멤버 상태 확인
        if (clubMember.getState() == ClubMemberState.JOINING) // 가입 중인 경우
            throw new ServiceException(400, "이미 가입 상태입니다.");
        else if (clubMember.getState() != ClubMemberState.INVITED) // 초대 상태가 아닌 경우 (가입 신청, 탈퇴)
            throw new ServiceException(400, "클럽 초대 상태가 아닙니다.");

        // 클럽 멤버 상태 업데이트
        if(accept) {
            clubMember.updateState(ClubMemberState.JOINING); // 초대 수락
        } else {
            club.getClubMembers().remove(clubMember); // 클럽에서 멤버 제거
            clubMemberRepository.delete(clubMember); // 초대 거절
        }

        return club; // 클럽 반환
    }

    /**
     * 클럽 가입 신청 메서드
     * @param clubId 클럽 ID
     * @return 클럽 정보
     */
    @Transactional
    public Club applyForClub(Long clubId) {
        // 멤버 가져오기
        Member user = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        // 클럽 ID로 클럽 가져오기
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 클럽이 비공개 상태인지 확인
        if (!club.isPublic()) {
            throw new ServiceException(403, "비공개 클럽입니다. 가입 신청이 불가능합니다.");
        }

        // 클럽 멤버 상태 확인
        if (clubMemberRepository.existsByClubAndMember(club, user)) {
            ClubMember existingMember = clubMemberRepository.findByClubAndMember(club, user)
                    .orElseThrow(() -> new ServiceException(404, "클럽 멤버가 존재하지 않습니다."));

            if (existingMember.getState() == ClubMemberState.JOINING)
                throw new ServiceException(400, "이미 가입 상태입니다.");
            else if (existingMember.getState() == ClubMemberState.APPLYING)
                throw new ServiceException(400, "이미 가입 신청 상태입니다.");
            else if (existingMember.getState() == ClubMemberState.INVITED)
                throw new ServiceException(400, "클럽 초대 상태입니다. 초대를 수락해주세요.");
            else if (existingMember.getState() == ClubMemberState.WITHDRAWN){
                // 탈퇴 상태면, 기존 클럽 멤버를 가져와서 APPLYING 상태로 변경
                existingMember.updateState(ClubMemberState.APPLYING); // 상태를 APPLYING으로 변경
                clubMemberRepository.save(existingMember); // 클럽 멤버 저장
                return club; // 클럽 반환
            }
        }

        // 클럽 멤버 생성 및 저장
        ClubMember clubMember = ClubMember.builder()
                .member(user)
                .role(ClubMemberRole.PARTICIPANT) // 기본 역할은 PARTICIPANT
                .state(ClubMemberState.APPLYING) // 가입 신청 상태로 설정
                .build();
        club.addClubMember(clubMember); // 클럽에 멤버 추가
        clubMemberRepository.save(clubMember); // 클럽 멤버 저장

        return club; // 클럽 반환
    }

    /**
     * 현재 로그인한 멤버의 클럽 정보 조회 메서드
     * @param clubId 클럽 ID
     * @return 클럽 멤버 정보
     */
    @Transactional(readOnly = true)
    public ClubMember getMyClubInfo(Long clubId) {
        // 현재 로그인한 멤버 가져오기
        Member user = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 클럽 ID로 클럽 가져오기
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 클럽 멤버 정보 조회
        return clubMemberRepository.findByClubAndMember(club, user)
                .orElseThrow(() -> new ServiceException(404, "클럽 멤버 정보가 존재하지 않습니다."));
    }

    public MyClubControllerDtos.MyClubList getMyClubs() {
        // 현재 로그인한 멤버 가져오기
        Member user = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 멤버가 속한 클럽 멤버 정보 조회
        List<ClubMember> clubMembers = clubMemberRepository.findAllByMember(user);

        // 클럽 멤버 정보를 기반으로 클럽 목록 생성
        List<MyClubControllerDtos.ClubListItem> clubListItems = clubMembers.stream()
                .map(clubMember -> {
                    Club club = clubMember.getClub();
                    return new MyClubControllerDtos.ClubListItem(
                            club.getId(),
                            club.getName(),
                            club.getBio(),
                            club.getCategory(),
                            club.getImageUrl(),
                            club.getMainSpot(),
                            club.getEventType(),
                            club.getStartDate(),
                            club.getEndDate(),
                            club.isPublic(),
                            clubMember.getRole(),
                            clubMember.getState()
                    );
                })
                .toList();

        // 클럽 목록을 MyClubList DTO로 반환
        return new MyClubControllerDtos.MyClubList(clubListItems);
    }

    public Club cancelClubApplication(Long clubId) {
        // 현재 로그인한 멤버 가져오기
        Member user = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 클럽 ID로 클럽 가져오기
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 클럽 멤버 정보 조회
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, user)
                .orElseThrow(() -> new ServiceException(404, "클럽 멤버 정보가 존재하지 않습니다."));

        // 클럽 멤버 상태 확인
        if (clubMember.getState() != ClubMemberState.APPLYING) {
            throw new ServiceException(400, "가입 신청 상태가 아닙니다.");
        }

        // 클럽에서 멤버 제거 및 클럽 멤버 삭제
        club.removeClubMember(clubMember);
        clubMemberRepository.delete(clubMember);

        return club; // 클럽 반환
    }
}
