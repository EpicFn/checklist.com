package com.back.domain.checkList.checkList.dto;

import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.checkList.itemAssign.dto.ItemAssignDto;
import com.back.global.enums.CheckListItemCategory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record CheckListItemDto(
    Long id,
    String content,
    CheckListItemCategory category,
    int sequence,
    boolean isChecked,
    List<ItemAssignDto> itemAssigns
) {
  public CheckListItemDto(CheckListItem checkListItem) {
    this(
        checkListItem.getId(),
        checkListItem.getContent(),
        checkListItem.getCategory(),
        checkListItem.getSequence(),
        checkListItem.isChecked(),
        Optional.ofNullable(checkListItem.getItemAssigns())
            .orElse(Collections.emptyList())
            .stream()
            .map(ItemAssignDto::new)
            .toList()
    );
  }
}
