package com.back.domain.member.member.dto.request;

public record UpdateMemberInfoDto(
        String nickname,
        String password,
        String bio
) {
}
