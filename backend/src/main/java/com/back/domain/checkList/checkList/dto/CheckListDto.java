package com.back.domain.checkList.checkList.dto;

import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.schedule.schedule.dto.response.ScheduleDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record CheckListDto(
    Long id,
    boolean isActive,
    ScheduleDto schedule,
    List<CheckListItemDto> checkListItems
) {
  public CheckListDto(CheckList checkList) {
    this(
        checkList.getId(),
        checkList.isActive(),
        new ScheduleDto(checkList.getSchedule()),
        Optional.ofNullable(checkList.getCheckListItems())
            .orElse(Collections.emptyList())
            .stream()
            .map(CheckListItemDto::new)
            .toList()
    );
  }
}
