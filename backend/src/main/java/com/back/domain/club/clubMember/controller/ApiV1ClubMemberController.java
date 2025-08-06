package com.back.domain.club.clubMember.controller;

import com.back.domain.club.clubMember.dtos.ClubMemberDtos;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 클럽 멤버 관련 API를 제공하는 컨트롤러
 * 클럽장 입장에서 멤버를 관리하는 기능을 포함한다
 */
@RestController
@RequestMapping("/api/v1/clubs/{clubId}/members")
@RequiredArgsConstructor
@Tag(name = "ClubMemberController", description = "클럽 멤버 관련 API")
public class ApiV1ClubMemberController {
    private final ClubMemberService clubMemberService;

    @PostMapping
    @Operation(summary = "클럽에 멤버 추가")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<Void> addMembersToClub(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubMemberDtos.ClubMemberRegisterRequest reqBody,
            @AuthenticationPrincipal SecurityUser user

    ) {
        clubMemberService.addMembersToClub(clubId, reqBody);

        return RsData.of(201, "클럽에 멤버가 추가됐습니다.", null);
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "클럽에서 멤버 탈퇴")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id) || @clubAuthorizationChecker.isSelf(#memberId, #user.id)")
    public RsData<Void> withdrawMemberFromClub(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        clubMemberService.withdrawMemberFromClub(clubId, memberId);

        return RsData.of(200, "클럽에서 멤버가 탈퇴됐습니다.", null);
    }

    @PutMapping("/{memberId}/role")
    @Operation(summary = "클럽 멤버 권한 변경")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<Void> changeMemberRole(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @RequestBody @Valid ClubMemberDtos.ClubMemberRoleChangeRequest reqBody,
            @AuthenticationPrincipal SecurityUser user
    ) {
        clubMemberService.changeMemberRole(clubId, memberId, reqBody.role());

        return RsData.of(200, "멤버의 권한이 변경됐습니다.", null);
    }

    @GetMapping
    @Operation(summary = "클럽 멤버 목록 조회")
    @PreAuthorize("@clubAuthorizationChecker.isClubMember(#clubId, #user.id)")
    public RsData<ClubMemberDtos.ClubMemberResponse> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(required = false) String state, // Optional: 상태 필터링
            @AuthenticationPrincipal SecurityUser user
    ) {
        ClubMemberDtos.ClubMemberResponse clubMemberResponse = clubMemberService.getClubMembers(clubId, state);

        return RsData.of(200, "클럽 멤버 목록이 조회됐습니다.", clubMemberResponse);
    }

    @PatchMapping("/{memberId}/approval")
    @Operation(summary = "클럽 가입 신청 승인")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<Void> approveMemberApplication(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        clubMemberService.handleMemberApplication(clubId, memberId, true);

        return RsData.of(200, "가입 신청이 승인됐습니다.", null);
    }

    @DeleteMapping("/{memberId}/approval")
    @Operation(summary = "클럽 가입 신청 거절")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<Void> rejectMemberApplication(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        clubMemberService.handleMemberApplication(clubId, memberId, false);

        return RsData.of(200, "가입 신청이 거절됐습니다.", null);
    }

}
