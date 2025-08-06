package com.back.domain.schedule.schedule.controller;

import com.back.domain.schedule.schedule.dto.request.ScheduleCreateReqBody;
import com.back.domain.schedule.schedule.dto.request.ScheduleUpdateReqBody;
import com.back.domain.schedule.schedule.dto.response.ScheduleDetailDto;
import com.back.domain.schedule.schedule.dto.response.ScheduleDto;
import com.back.domain.schedule.schedule.dto.response.ScheduleWithClubDto;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.service.ScheduleService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "ApiV1ScheduleController", description = "일정 컨트롤러")
public class ApiV1ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/clubs/{clubId}")
    @Operation(summary = "모임의 일정 목록 조회", description = "모임의 일정 목록 조회는 모임의 참여자만 가능")
    @PreAuthorize("@clubAuthorizationChecker.isClubMember(#clubId, #user.getId())")
    public RsData<List<ScheduleDto>> getClubSchedules(
            @PathVariable Long clubId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal SecurityUser user
    ) {
        List<ScheduleDto> schedules = scheduleService.getClubSchedules(clubId, startDate, endDate);

        return RsData.of(
                200,
                "일정 목록이 조회되었습니다.",
                schedules
        );
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 조회", description = "일정 조회는 모임의 참여자만 가능")
    @PreAuthorize("@scheduleAuthorizationChecker.isClubMember(#scheduleId, #user.getId())")
    public RsData<ScheduleDetailDto> getSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        ScheduleDetailDto schedule = scheduleService.getActiveScheduleById(scheduleId);

        return RsData.of(
                200,
                "일정이 조회되었습니다.",
                schedule
        );
    }

    @PostMapping
    @Operation(summary = "일정 생성", description = "일정 생성은 호스트 또는 매니저 권한이 있는 사용자만 가능")
    @PreAuthorize("@clubAuthorizationChecker.isActiveClubManagerOrHost(#reqBody.clubId, #user.getId())")
    public RsData<ScheduleDetailDto> createSchedule(
            @Valid @RequestBody ScheduleCreateReqBody reqBody,
            @AuthenticationPrincipal SecurityUser user
    ) {
        ScheduleDetailDto schedule = scheduleService.createSchedule(reqBody);

        return RsData.of(
                201,
                "일정이 생성되었습니다.",
                schedule
        );
    }

    @PutMapping("{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 수정은 호스트 또는 매니저 권한이 있는 사용자만 가능")
    @PreAuthorize("@scheduleAuthorizationChecker.isActiveClubManagerOrHost(#scheduleId, #user.getId())")
    public RsData<ScheduleDetailDto> modifySchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateReqBody reqBody,
            @AuthenticationPrincipal SecurityUser user
    ) {
        Schedule schedule = scheduleService.getActiveScheduleEntityById(scheduleId);
        scheduleService.modifySchedule(schedule, reqBody);

        return RsData.of(
                200,
                "일정이 수정되었습니다.",
                new ScheduleDetailDto(schedule)
        );
    }

    @DeleteMapping("{scheduleId}")
    @Operation(summary = "일정 삭제", description = "일정 삭제는 호스트 또는 매니저 권한이 있는 사용자만 가능")
    @PreAuthorize("@scheduleAuthorizationChecker.isActiveClubManagerOrHost(#scheduleId, #user.getId())")
    public RsData<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        Schedule schedule = scheduleService.getActiveScheduleEntityById(scheduleId);
        scheduleService.deleteSchedule(schedule);

        return RsData.of(
                200,
                "일정이 삭제되었습니다."
        );
    }

    @GetMapping("/me")
    @Operation(summary = "나의 일정 목록 조회")
    public RsData<List<ScheduleWithClubDto>> getMySchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        List<ScheduleWithClubDto> mySchedules = scheduleService.getMySchedules(user.getId(), startDate, endDate);
        return RsData.of(
                200,
                "나의 일정 목록이 조회되었습니다.",
                mySchedules
        );
    }
}
