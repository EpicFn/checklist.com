package com.back.domain.club.clubLink.controller;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.service.ClubLinkService;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubApplyResult;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class ApiV1ClubLinkController {
    private final Rq rq;
    private final ClubLinkService clubLinkService;

    @PostMapping("/{clubId}/members/invitation-link")
    @Operation(summary = "클럽 초대 링크 생성")
    public RsData<ClubLinkDtos.CreateClubLinkResponse> createClubLink(@PathVariable @Positive Long clubId) {

        Member user = rq.getActor();
        ClubLinkDtos.CreateClubLinkResponse response = clubLinkService.createClubLink(user, clubId);

        return new RsData<>(200, "클럽 초대 링크가 생성되었습니다.", response);
    }

    @GetMapping("/{clubId}/members/invitation-link")
    @Operation(summary = "클럽 초대 링크 반환")
    public RsData<ClubLinkDtos.CreateClubLinkResponse> getExistingClubLink(@PathVariable @Positive Long clubId) {

        Member user = rq.getActor();
        ClubLinkDtos.CreateClubLinkResponse response = clubLinkService.getExistingClubLink(user, clubId);

        return new RsData<>(200, "클럽 초대 링크가 반환되었습니다.", response);
    }

    @PostMapping("/invitations/{token}/apply")
    @Operation(summary = "로그인 유저 - 초대 링크를 통한 비공개 클럽 가입 신청")
    public RsData<Object> applyToClubByInvitationToken(@PathVariable String token) {
        Member user = rq.getActor();
        ClubApplyResult result = clubLinkService.applyToPrivateClub(user, token);

        return switch (result) {
            case SUCCESS -> new RsData<>(200, "클럽 가입 신청이 성공적으로 완료되었습니다.", null);
            case ALREADY_JOINED -> new RsData<>(400, "이미 이 클럽에 가입되어 있습니다.", null);
            case ALREADY_APPLYING -> new RsData<>(400, "이미 가입 신청 중입니다.", null);
            case ALREADY_INVITED -> new RsData<>(400, "이미 초대를 받은 상태입니다. 마이페이지에서 수락해주세요.", null);
            case TOKEN_EXPIRED -> new RsData<>(400, "초대 토큰이 만료되었습니다.", null);
            case TOKEN_INVALID -> new RsData<>(400, "초대 토큰이 유효하지 않습니다.", null);
        };
    }

    @GetMapping("/invitations/{token}")
    @Operation(summary = "클럽 초대 링크용 클럽 정보 반환")
    public RsData<ClubControllerDtos.SimpleClubInfoResponse> getClubInfoByInvitationToken(@PathVariable String token) {
        ClubControllerDtos.SimpleClubInfoResponse response = clubLinkService.getClubInfoByInvitationToken(token);

        return new RsData<>(200, "클럽 정보가 반환되었습니다.", response);
    }
}
