package com.back.domain.preset.preset.dto;

import com.back.domain.preset.preset.entity.Preset;

import java.util.List;

public record PresetDto(
    Long id,
    String name,
    List<PresetItemDto> presetItems
) {
  public PresetDto(Preset preset) {
    this(
        preset.getId(),
        preset.getName(),
        preset.getPresetItems().stream()
            .map(PresetItemDto::new)
            .toList()
    );
  }
}
