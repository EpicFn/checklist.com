package com.back.domain.member.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberWithdrawMembershipResponse(
        @Schema(description = "회원 닉네임", example = "testUser1")
        String nickname,

        @Schema(description = "회원 태그", example = "2345")
        String tag
) {
}
