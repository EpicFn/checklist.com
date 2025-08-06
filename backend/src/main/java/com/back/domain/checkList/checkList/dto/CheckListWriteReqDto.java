package com.back.domain.checkList.checkList.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckListWriteReqDto(
    @NotNull(message = "일정 ID는 필수입니다.")
    Long scheduleId,
    @Valid
    List<CheckListItemWriteReqDto> checkListItems
) {
}
