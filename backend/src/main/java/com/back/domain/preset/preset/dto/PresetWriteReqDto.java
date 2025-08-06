package com.back.domain.preset.preset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PresetWriteReqDto(
    @NotBlank(message = "프리셋 이름은 필수입니다")
    String name,
    @Valid
    List<PresetItemWriteReqDto> presetItems
) {
}
