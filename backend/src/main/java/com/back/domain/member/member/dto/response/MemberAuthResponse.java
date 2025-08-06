package com.back.domain.member.member.dto.response;

public record MemberAuthResponse(
        String apikey,
        String accessToken
) {}
