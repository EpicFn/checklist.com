package com.back.domain.member.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuestDto(
        @NotBlank (message = "닉네임은 필수 입력값입니다.")
        String nickname,
        @NotBlank (message = "비밀번호는 필수 입력값입니다.")
        String password,
        @NotNull (message = "클럽 id는 필수 입력값입니다.")
        Long clubId
) {
}
