package com.back.domain.member.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordCheckRequestDto(
        @NotBlank (message = "비밀번호는 필수 입력값입니다.")
        String password
        ) {
}
