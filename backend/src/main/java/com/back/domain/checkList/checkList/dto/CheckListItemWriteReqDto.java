package com.back.domain.checkList.checkList.dto;

import com.back.domain.checkList.itemAssign.dto.ItemAssignReqDto;
import com.back.global.enums.CheckListItemCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckListItemWriteReqDto(
    @NotBlank(message = "내용은 필수입니다.")
    String content,
    @NotNull(message = "카테고리는 필수입니다.")
    CheckListItemCategory category,
    int sequence,
    boolean isChecked,
    @Valid
    List<ItemAssignReqDto> itemAssigns
) {
}
