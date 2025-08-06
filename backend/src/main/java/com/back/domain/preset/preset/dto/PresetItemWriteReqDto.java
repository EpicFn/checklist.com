package com.back.domain.preset.preset.dto;

import com.back.global.enums.CheckListItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresetItemWriteReqDto(
    @NotBlank(message = "내용은 필수입니다.")
    String content,
    @NotNull(message = "카테고리는 필수입니다.")
    CheckListItemCategory category,
    int sequence
) {
}
