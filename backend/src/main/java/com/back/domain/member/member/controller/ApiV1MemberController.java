package com.back.domain.member.member.controller;

import com.back.domain.api.request.TokenRefreshRequest;
import com.back.domain.member.member.dto.request.*;
import com.back.domain.member.member.dto.response.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.jsonwebtoken.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 관련 API를 제공하는 컨트롤러입니다.
 * - 회원가입 / 로그인 / 로그아웃 / 탈퇴 / 정보 조회 및 수정
 * - 비회원 등록 / 비회원 로그인
 * - Access Token 재발급 등 인증 관련 처리 포함
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class ApiV1MemberController {
    private final MemberService memberService;
    private final Rq rq;

    // ================= 회원용 API =================
    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호 등을 받아 회원가입을 처리합니다.")
    @PostMapping("/auth/register")
    public RsData<MemberAuthResponse> register(@Valid @RequestBody MemberRegisterDto memberRegisterDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.registerMember(memberRegisterDto);

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken(), false);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "회원가입 성공", memberAuthResponse);
    }

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 로그인을 처리합니다.")
    @PostMapping("/auth/login")
    public RsData<MemberAuthResponse> login(@Valid @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.loginMember(memberLoginDto);

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken(), false);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "로그인 성공", memberAuthResponse);
    }

    @Operation(summary = "로그아웃 API", description = "로그아웃 처리 API입니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/auth/logout")
    public RsData<MemberAuthResponse> logout(HttpServletResponse response) {
        Cookie expiredCookie = deleteCookie();

        response.addCookie(expiredCookie);

        return RsData.of(200, "로그아웃 성공");
    }

    @Operation(summary = "회원탈퇴 API", description = "회원탈퇴 처리 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public RsData<MemberWithdrawMembershipResponse> withdrawMembership(HttpServletResponse response) {
        Member user = rq.getActor();

        if (user == null) {
            throw new ServiceException(401, "인증이 필요합니다.");
        }

        MemberWithdrawMembershipResponse responseDto =
                memberService.withdrawMember(user.getNickname(), user.getTag());

        response.addCookie(deleteCookie());

        return RsData.of(200,
                "회원탈퇴 성공",
                responseDto);
    }


    // ================= 내 정보 관련 API =================
    @Operation(summary = "내 정보 반환 API", description = "현재 로그인한 유저 정보를 반환하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public RsData<MemberDetailInfoResponse> getMyInfo(HttpServletResponse response) {
        Member user = rq.getActor();

        if (user == null) {
            throw new ServiceException(401, "인증이 필요합니다.");
        }

        MemberDetailInfoResponse memberDetailInfoResponse =
                memberService.getMemberInfo(user.getId());

        return RsData.of(200,
                "유저 정보 반환 성공",
                memberDetailInfoResponse);
    }

    @Operation(summary = "내 정보 수정 API", description = "현재 로그인한 유저 정보를 수정하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public RsData<MemberDetailInfoResponse> updateInfo(@RequestPart(value = "data") UpdateMemberInfoDto dto,
                                                       @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        Member user = rq.getActor();
        if (user == null) {
            throw new ServiceException(401, "인증이 필요합니다.");
        }

        MemberDetailInfoResponse memberDetailInfoResponse =
                memberService.updateMemberInfo(user.getId(), dto, profileImage);

        return RsData.of(200,
                "유저 정보 수정 성공",
                memberDetailInfoResponse);
    }


    // ================= 비회원용 API =================
    @Operation(summary = "비회원 모임 등록 API", description = "비회원 모임 등록 API 입니다.")
    @PostMapping("/auth/guest-register")
    public RsData<GuestResponse> registerGuest(HttpServletResponse response,
                                               @Valid @RequestBody GuestDto dto) {
        GuestResponse guestResponse =
                memberService.registerGuestMember(dto);

        Cookie accessTokenCookie = createAccessTokenCookie(guestResponse.accessToken(), true);

        response.addCookie(accessTokenCookie);

        return RsData.of(200,
                "비회원 모임 가입 성공",
                guestResponse);
    }

    @Operation(summary = "비회원 임시 로그인 API", description = "비회원 임시 로그인 API 입니다.")
    @PostMapping("/auth/guest-login")
    public RsData<GuestResponse> guestLogin(HttpServletResponse response,
                                            @Valid @RequestBody GuestDto guestDto) {
        GuestResponse guestAuthResponse = memberService.loginGuestMember(guestDto);

        Cookie accessTokenCookie = createAccessTokenCookie(guestAuthResponse.accessToken(), true);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "비회원 로그인 성공", guestAuthResponse);
    }


    // ================= 회원, 비회원용 쿠키 생성/삭제 =================
    private Cookie createAccessTokenCookie(String accessToken, boolean isGuest) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        cookie.setMaxAge(isGuest ? 60 * 60 * 24 * 30 : 60 * 60 * 24);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    private Cookie deleteCookie() {
        Cookie expiredCookie = new Cookie("accessToken", "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);

        return expiredCookie;
    }

    // ================= 기타 API =================
    @Operation(summary = "비밀번호 유효성 검사 API", description = "비밀번호의 유효성을 인증하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auth/verify-password")
    public RsData<MemberPasswordResponse> checkPasswordValidity(@Valid @RequestBody PasswordCheckRequestDto dto) {

        Member user = rq.getActor();

        MemberPasswordResponse response = memberService.checkPasswordValidity(user.getId(), dto.password());

        return RsData.of(200,
                "비밀번호 유효성 반환 성공",
                response);
    }

    @Operation(summary = "access token 재발급 API", description = "리프레시 토큰으로 access token을 재발급하는 API 입니다.")
    @PostMapping("/auth/refresh")
    public RsData<MemberAuthResponse> apiTokenReissue(@RequestBody TokenRefreshRequest requestBody,
                                                      HttpServletResponse response) {

        String apiKey = requestBody.refreshToken();

        if (apiKey == null || apiKey.isBlank()) {
            return RsData.of(401, "Refresh Token이 존재하지 않습니다.");
        }

        // 사용자 정보 추출
        Member member = memberService.findMemberByApiKey(apiKey);

        // 새로운 access token 생성
        String newAccessToken = memberService.generateAccessToken(member);

        // access token 쿠키에 담아서 응답
        Cookie accessTokenCookie = createAccessTokenCookie(newAccessToken, false);
        response.addCookie(accessTokenCookie);

        return RsData.of(200, "Access Token 재발급 성공",
                new MemberAuthResponse(apiKey, newAccessToken));
    }
}

//@GetMapping("/{scheduleId}")
//    @Operation(summary = "일정 조회")
//    public RsData<ScheduleDto> getSchedule(
//            @PathVariable Long scheduleId
//    ) {
//        Schedule schedule = scheduleService.getScheduleById(scheduleId);
//        return RsData.of(
//                200,
//                "%s번 일정이 조회되었습니다.".formatted(scheduleId),
//                new ScheduleDto(schedule)
//        );
//    }