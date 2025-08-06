package com.back.domain.preset.preset.dto;

import com.back.domain.preset.preset.entity.PresetItem;
import com.back.global.enums.CheckListItemCategory;

public record PresetItemDto(
    Long id,
    String content,
    CheckListItemCategory category,
    int sequence
) {
  public PresetItemDto(PresetItem presetItem) {
    this(
        presetItem.getId(),
        presetItem.getContent(),
        presetItem.getCategory(),
        presetItem.getSequence()
    );
  }
}
