package com.back.domain.member.member.dto.response;

public record MemberDetailInfoResponse(
        String nickname,
        String email,
        String bio,
        String profileImage,
        String tag
) {
}
