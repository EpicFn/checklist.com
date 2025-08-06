package com.back.domain.schedule.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleUpdateReqBody(
        @Schema(description = "일정 제목")
        @NotEmpty String title,

        @Schema(description = "일정 내용")
        @NotEmpty String content,

        @Schema(description = "일정 시작일")
        @NotNull LocalDateTime startDate,

        @Schema(description = "일정 종료일")
        @NotNull LocalDateTime endDate,

        @Schema(description = "일정 장소")
        @NotNull String spot
) {
}
