package com.back.domain.member.member.error;

import com.back.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND("404", "사용자를 찾을 수 없습니다.");

    private final int status;
    private final String message;

    MemberErrorCode(String status, String message) {
        this.status = Integer.parseInt(status);
        this.message = message;
    }
}
