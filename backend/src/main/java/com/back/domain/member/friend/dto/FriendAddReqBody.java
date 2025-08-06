package com.back.domain.member.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record FriendAddReqBody(
        @Schema(description = "친구 요청 대상의 이메일")
        @NotNull @Email String friend_email
) {
}
