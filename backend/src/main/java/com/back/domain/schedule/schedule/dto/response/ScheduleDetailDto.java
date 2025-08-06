package com.back.domain.schedule.schedule.dto.response;

import com.back.domain.schedule.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ScheduleDetailDto(
        @Schema(description = "일정 ID")
        Long id,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "일정 내용")
        String content,
        @Schema(description = "일정 시작일")
        LocalDateTime startDate,
        @Schema(description = "일정 종료일")
        LocalDateTime endDate,
        @Schema(description = "일정 장소")
        String spot,
        @Schema(description = "모임 ID")
        Long clubId,
        @Schema(description = "체크리스트 ID", nullable = true)
        Long checkListId
) {
    public ScheduleDetailDto(Schedule schedule) {
        this(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getContent(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getSpot(),
                schedule.getClub().getId(),
                schedule.getCheckList() != null ? schedule.getCheckList().getId() : null
        );
    }
}
