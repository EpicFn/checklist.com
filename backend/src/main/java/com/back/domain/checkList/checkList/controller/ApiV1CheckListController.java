package com.back.domain.checkList.checkList.controller;

import com.back.domain.checkList.checkList.dto.CheckListDto;
import com.back.domain.checkList.checkList.dto.CheckListUpdateReqDto;
import com.back.domain.checkList.checkList.dto.CheckListWriteReqDto;
import com.back.domain.checkList.checkList.service.CheckListService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Tag(name="ApiV1CheckListController", description="체크리스트 API V1 컨트롤러")
public class ApiV1CheckListController {
  private final CheckListService checkListService;

  @PostMapping
  @Operation(summary = "체크리스트 생성")
  @PreAuthorize("@checkListAuthorizationChecker.isActiveClubManagerOrHostByScheduleId(#checkListWriteReqDto.scheduleId(), #user.getId())")
  public ResponseEntity<RsData<CheckListDto>> write(@Valid @RequestBody CheckListWriteReqDto checkListWriteReqDto, @AuthenticationPrincipal SecurityUser user) {
    System.out.println("casdasadad");
    RsData<CheckListDto> checkListDto = checkListService.write(checkListWriteReqDto);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }

  @GetMapping("/{checkListId}")
  @Operation(summary = "체크리스트 조회")
  @PreAuthorize("@checkListAuthorizationChecker.isClubMember(#checkListId, #user.getId())")
  public ResponseEntity<RsData<CheckListDto>> getCheckList(@PathVariable Long checkListId, @AuthenticationPrincipal SecurityUser user) {
    RsData<CheckListDto> checkListDto = checkListService.getCheckList(checkListId);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }

  @PutMapping("/{checkListId}")
  @Operation(summary = "체크리스트 수정")
  @PreAuthorize("@checkListAuthorizationChecker.isActiveClubManagerOrHost(#checkListId, #user.getId())")
  public ResponseEntity<RsData<CheckListDto>> updateCheckList(@PathVariable Long checkListId, @Valid @RequestBody CheckListUpdateReqDto checkListUpdateReqDto, @AuthenticationPrincipal SecurityUser user) {
    RsData<CheckListDto> checkListDto = checkListService.updateCheckList(checkListId, checkListUpdateReqDto);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }

  @DeleteMapping("/{checkListId}")
  @Operation(summary = "체크리스트 삭제")
  @PreAuthorize("@checkListAuthorizationChecker.isActiveClubManagerOrHost(#checkListId, #user.getId())")
  public ResponseEntity<RsData<CheckListDto>> deleteCheckList(@PathVariable Long checkListId, @AuthenticationPrincipal SecurityUser user) {
    RsData<CheckListDto> checkListDto = checkListService.deleteCheckList(checkListId);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }

  @GetMapping("/group/{groupId}")
  @Operation(summary = "체크리스트 목록 조회")
  @PreAuthorize("@clubAuthorizationChecker.isClubMember(#groupId, #user.getId())")
  public ResponseEntity<RsData<List<CheckListDto>>> getCheckListByGroupId(@PathVariable Long groupId, @AuthenticationPrincipal SecurityUser user) {
    RsData<List<CheckListDto>> checkListDtos = checkListService.getCheckListByGroupId(groupId);

    return ResponseEntity.status(checkListDtos.code()).body(checkListDtos);
  }
}
