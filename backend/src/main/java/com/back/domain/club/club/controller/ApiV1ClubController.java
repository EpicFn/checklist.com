package com.back.domain.club.club.controller;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
@Tag(name = "ClubController", description = "클럽 관련 API")
public class ApiV1ClubController {
    private final ClubService clubService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "클럽 생성")
    public RsData<ClubControllerDtos.ClubResponse> createClub(
            @Valid @RequestPart("data") ClubControllerDtos.CreateClubRequest reqBody,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Club club = clubService.createClub(reqBody, image);

        return new RsData<>(201, "클럽이 생성됐습니다.",
                new ClubControllerDtos.ClubResponse(
                        club.getId(),
                        club.getLeaderId()
                )
        );
    }

    @PatchMapping("/{clubId}")
    @Operation(summary = "클럽 수정")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<ClubControllerDtos.ClubResponse> updateClubInfo(
            @PathVariable Long clubId,
            @Valid @RequestPart("data") ClubControllerDtos.UpdateClubRequest reqBody,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal SecurityUser user
    ) throws IOException {
        Club club = clubService.updateClub(clubId, reqBody, image);

        return new RsData<>(200, "클럽 정보가 수정됐습니다.",
                new ClubControllerDtos.ClubResponse(
                        club.getId(),
                        club.getLeaderId()
                )
        );
    }

    @DeleteMapping("/{clubId}")
    @Operation(summary = "클럽 삭제")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubHost(#clubId, #user.id)")
    public RsData<Void> deleteClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        clubService.deleteClub(clubId);
        return new RsData<>(204, "클럽이 삭제됐습니다.", null);
    }

    @GetMapping("/{clubId}")
    @Operation(summary = "클럽 정보 조회")
    public RsData<ClubControllerDtos.ClubInfoResponse> getClubInfo(@PathVariable Long clubId) {
        ClubControllerDtos.ClubInfoResponse info = clubService.getClubInfo(clubId);
        return new RsData<>(200, "클럽 정보가 조회됐습니다.", info);
    }

    @GetMapping("/public")
    @Operation(summary = "공개 클럽 목록 조회 (페이징 가능)")
    public RsData<Page<ClubControllerDtos.SimpleClubInfoWithoutLeader>> getPublicClubs(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mainSpot,
            @RequestParam(required = false) ClubCategory category,
            @RequestParam(required = false) EventType eventType
    ) {
        Page<ClubControllerDtos.SimpleClubInfoWithoutLeader> response = clubService.getPublicClubs(pageable, name, mainSpot, category, eventType);
        return new RsData<>(200, "공개 클럽 목록이 조회됐습니다.", response);
    }
}
