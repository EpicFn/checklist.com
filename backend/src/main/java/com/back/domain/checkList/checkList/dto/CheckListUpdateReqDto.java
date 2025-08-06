package com.back.domain.checkList.checkList.dto;

import jakarta.validation.Valid;

import java.util.List;

public record CheckListUpdateReqDto(
    @Valid
    List<CheckListItemWriteReqDto> checkListItems
) {
}
