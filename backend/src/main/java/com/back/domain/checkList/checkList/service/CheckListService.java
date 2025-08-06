package com.back.domain.checkList.checkList.service;

import com.back.domain.checkList.checkList.dto.CheckListDto;
import com.back.domain.checkList.checkList.dto.CheckListUpdateReqDto;
import com.back.domain.checkList.checkList.dto.CheckListWriteReqDto;
import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.checkList.checkList.repository.CheckListRepository;
import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.club.clubMember.service.ClubMemberValidService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckListService {
  private final CheckListRepository checkListRepository;
  private final ScheduleRepository scheduleRepository;
  private final ClubRepository clubRepository;
  private final Rq rq;

  // 체커에 사용되는 메서드
  public CheckList getActiveCheckListById(Long checkListId) {
    // 활성화된 체크리스트 조회
    return checkListRepository
        .findActiveCheckListById(checkListId)
        .orElseThrow(() -> new NoSuchElementException("체크리스트를 찾을 수 없습니다"));
  }

  @Transactional
  public RsData<CheckListDto> write(CheckListWriteReqDto checkListWriteReqDto) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 전달 받은 checkListWriteReqDto에서 scheduleId로 Schedule 엔티티 조회
    Optional<Schedule> otnSchedule = scheduleRepository.findById(checkListWriteReqDto.scheduleId());
//    if (otnSchedule.isEmpty()) return RsData.of(404, "일정을 찾을 수 없습니다");
    Schedule schedule = otnSchedule.get();


    // Schedule에 CheckList가 이미 존재하는 경우 RsData 반환
//    if (schedule.getCheckList() != null) return RsData.of(409, "이미 체크리스트가 존재합니다");

    // Schedule 엔티티에서 클럽 조회 멤버 조회
    Optional<ClubMember> otnClubMember = schedule.getClub().getClubMembers().stream()
        .filter(clubMember ->
            clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아니거나 가입중이 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) return RsData.of(403, "클럽 멤버가 아닙니다");

    if (otnClubMember.get().getRole().equals(ClubMemberRole.PARTICIPANT)) return RsData.of(403, "호스트 또는 관리자만 체크리스트를 생성할 수 있습니다");
    List<CheckListItem> checkListItems;
    // 전달 받은 checkListWriteReqDto에서 Request받은 CheckListItem를 CheckListItem 엔티티로 변환 해서 리스트로 변환
    try {
      checkListItems = checkListWriteReqDto.checkListItems().stream()
          .map(req -> CheckListItem.builder()
              .content(req.content())
              .category(req.category())
              .sequence(req.sequence())
              .itemAssigns(Optional.ofNullable(req.itemAssigns())
                  .orElse(Collections.emptyList())
                  .stream()
                  .map(itemAssignReq -> {
                    // 클럽 멤버 조회 (Optional 안전 처리)
                    ClubMember clubMember = schedule.getClub().getClubMembers().stream().filter(
                            cm -> cm.getId().equals(itemAssignReq.clubMemberId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("클럽 멤버를 찾을 수 없습니다"));

                    return ItemAssign.builder()
                        .clubMember(clubMember)
                        .build();
                  })
                  .collect(Collectors.toList()))
              .build())
          .collect(Collectors.toList());
    }catch (IllegalArgumentException e) {
      return RsData.of(403, e.getMessage());
    }
    // CheckList 엔티티 생성
    CheckList checkListBuilder = CheckList.builder()
        .isActive(true)
        .schedule(schedule)
        .checkListItems(checkListItems)
        .build();

    // CheckList 엔티티 저장
    CheckList checkList = checkListRepository.save(checkListBuilder);
    schedule.updateCheckList(checkList); // Schedule과 CheckList 연관 설정
    scheduleRepository.save(schedule); // Schedule 업데이트
    // CheckListDto로 변환
    CheckListDto checkListDto = new CheckListDto(checkList);

    return RsData.of(201, "체크리스트 생성 성공", checkListDto);
  }

  public RsData<CheckListDto> getCheckList(Long checkListId) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 체크리스트 ID로 체크리스트 조회
    Optional<CheckList> otnCheckList = checkListRepository.findById(checkListId);

    // 체크리스트가 존재하지 않는 경우 RsData 반환
//    if (otnCheckList.isEmpty()) return RsData.of(404, "체크리스트를 찾을 수 없습니다");
    CheckList checkList = otnCheckList.get();

    // 체크리스트의 연동된 일정이 존재하지 않는 경우 RsData 반환
//    if (checkList.getSchedule() == null) return RsData.of(404, "체크리스트에 연동된 일정이 없습니다");

    // 체크리스트의 연동된 일정의 클럽 멤버 조회
    Optional<ClubMember> otnClubMember = checkList.getSchedule().getClub().getClubMembers().stream()
        .filter(clubMember -> clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) return RsData.of(403, "클럽 멤버가 아닙니다");

    // 체크리스트가 존재하는 경우 체크리스트 DTO로 변환
    CheckListDto checkListDto = new CheckListDto(checkList);

    return RsData.of(200, "체크리스트 조회 성공", checkListDto);
  }

  @Transactional
  public RsData<CheckListDto> updateCheckList(Long checkListId, CheckListUpdateReqDto checkListUpdateReqDto) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 체크리스트 ID로 체크리스트 조회
    Optional<CheckList> otnCheckList = checkListRepository.findById(checkListId);

    // 체크리스트가 존재하지 않는 경우 RsData 반환
//    if (otnCheckList.isEmpty()) return RsData.of(404, "체크리스트를 찾을 수 없습니다");
    CheckList checkList = otnCheckList.get();

    // 체크리스트의 연동된 일정이 존재하지 않는 경우 RsData 반환
//    if (checkList.getSchedule() == null) return RsData.of(404, "체크리스트에 연동된 일정이 없습니다");

    // 체크리스트의 연동된 일정의 클럽 멤버 조회
    Optional<ClubMember> otnClubMember = checkList.getSchedule().getClub().getClubMembers().stream()
        .filter(clubMember -> clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) return RsData.of(403, "클럽 멤버가 아닙니다");

    // 클럽 멤버의 역할이 PARTICIPANT인 경우 RsData 반환
    if (otnClubMember.get().getRole().equals(ClubMemberRole.PARTICIPANT)) return RsData.of(403, "호스트 또는 관리자만 체크리스트를 수정할 수 있습니다");
    List<CheckListItem> checkListItems;
    try {
      checkListItems = checkListUpdateReqDto.checkListItems().stream()
          .map(req -> CheckListItem.builder()
              .content(req.content())
              .category(req.category())
              .sequence(req.sequence())
              .isChecked(req.isChecked())
              .checkList(checkList)
              .itemAssigns(Optional.ofNullable(req.itemAssigns())
                  .orElse(Collections.emptyList())
                  .stream()
                  .map(itemAssignReq -> {
                    // 클럽 멤버 조회 (Optional 안전 처리)
                    ClubMember clubMember = checkList.getSchedule().getClub().getClubMembers().stream().filter(
                            cm -> cm.getId().equals(itemAssignReq.clubMemberId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("클럽 멤버를 찾을 수 없습니다"));

                    return ItemAssign.builder()
                        .clubMember(clubMember)
                        .isChecked(itemAssignReq.isChecked())
                        .build();
                  }).collect(Collectors.toList()))
              .build())
          .collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      return RsData.of(403, e.getMessage());
    }
    // CheckList의 아이템 업데이트
    checkList.updateCheckListItems(checkListItems);

    // CheckList 엔티티 저장
    CheckList updatedCheckList = checkListRepository.save(checkList);
    // CheckListDto로 변환
    CheckListDto checkListDto = new CheckListDto(updatedCheckList);
    return RsData.of(200, "체크리스트 수정 성공", checkListDto);
  }

  @Transactional
  public RsData<CheckListDto> deleteCheckList(Long checkListId) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 체크리스트 ID로 체크리스트 조회
    Optional<CheckList> otnCheckList = checkListRepository.findById(checkListId);

    // 체크리스트가 존재하지 않는 경우 RsData 반환
//    if (otnCheckList.isEmpty()) return RsData.of(404, "체크리스트를 찾을 수 없습니다");
    CheckList checkList = otnCheckList.get();

    // 체크리스트의 연동된 일정이 존재하지 않는 경우 RsData 반환
//    if (checkList.getSchedule() == null) return RsData.of(404, "체크리스트에 연동된 일정이 없습니다");

    // 체크리스트의 연동된 일정의 클럽 멤버 조회
    Optional<ClubMember> otnClubMember = checkList.getSchedule().getClub().getClubMembers().stream()
        .filter(clubMember -> clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) return RsData.of(403, "클럽 멤버가 아닙니다");

    // 클럽 멤버의 역할이 PARTICIPANT인 경우 RsData 반환
    if (otnClubMember.get().getRole().equals(ClubMemberRole.PARTICIPANT)) return RsData.of(403, "호스트 또는 관리자만 체크리스트를 삭제할 수 있습니다");

    // CheckList 삭제
    checkListRepository.delete(checkList);

    return RsData.of(200, "체크리스트 삭제 성공", new CheckListDto(checkList));
  }

  public RsData<List<CheckListDto>> getCheckListByGroupId(Long groupId) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 클럽 ID로 클럽 조회
    Optional<Club> otnClub = clubRepository.findById(groupId);
    if (otnClub.isEmpty()) return RsData.of(404, "클럽을 찾을 수 없습니다");
    Club club = otnClub.get();

    // 클럽 멤버 조회
    Optional<ClubMember> otnClubMember = club.getClubMembers().stream()
        .filter(clubMember -> clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) {
      return RsData.of(403, "클럽 멤버가 아닙니다");
    }

    // 클럽의 체크리스트 조회
    List<CheckList> checkLists = otnClubMember.get().getClub().getClubSchedules().stream()
        .map(Schedule::getCheckList)
        .filter(checkList -> checkList != null && checkList.isActive())
        .toList();

    otnClubMember.get().getClub().getClubSchedules().forEach(schedule -> {
      System.out.println("Schedule ID: " + schedule.getId() + ", CheckList: " + schedule.getCheckList());
    });

    List<CheckListDto> checkListDtos = checkLists.stream()
        .map(CheckListDto::new)
        .collect(Collectors.toList());
    return RsData.of(200, "체크리스트 목록 조회 성공", checkListDtos);
  }

}
