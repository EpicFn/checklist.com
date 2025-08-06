package com.back.domain.api.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "리프레시 토큰은 필수 입력값입니다.")
        String refreshToken) {
}
