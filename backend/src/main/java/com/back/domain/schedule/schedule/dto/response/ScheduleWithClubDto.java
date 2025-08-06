package com.back.domain.schedule.schedule.dto.response;

import com.back.domain.schedule.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ScheduleWithClubDto(
        @Schema(description = "일정 ID")
        Long id,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "일정 시작일")
        LocalDateTime startDate,
        @Schema(description = "일정 종료일")
        LocalDateTime endDate,
        @Schema(description = "모임 ID")
        Long clubId,
        @Schema(description = "모임명")
        String clubName,
        @Schema(description = "체크리스트 ID", nullable = true)
        Long checkListId
) {
    public ScheduleWithClubDto(Schedule schedule) {
        this(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getClub().getId(),
                schedule.getClub().getName(),
                schedule.getCheckList() != null ? schedule.getCheckList().getId() : null
        );
    }
}
