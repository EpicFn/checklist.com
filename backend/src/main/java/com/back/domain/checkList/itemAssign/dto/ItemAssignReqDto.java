package com.back.domain.checkList.itemAssign.dto;

import jakarta.validation.constraints.NotNull;

public record ItemAssignReqDto(
    @NotNull(message = "클럽 멤버 ID는 필수입니다.")
    Long clubMemberId,
    boolean isChecked
) {
}
