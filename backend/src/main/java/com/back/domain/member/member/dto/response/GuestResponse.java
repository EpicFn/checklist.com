package com.back.domain.member.member.dto.response;

public record GuestResponse(
        String nickname,
        String accessToken,
        Long clubId
) {
}
