package com.back.domain.schedule.schedule.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.error.ClubErrorCode;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.schedule.schedule.dto.DateTimeRange;
import com.back.domain.schedule.schedule.dto.request.ScheduleCreateReqBody;
import com.back.domain.schedule.schedule.dto.request.ScheduleUpdateReqBody;
import com.back.domain.schedule.schedule.dto.response.ScheduleDetailDto;
import com.back.domain.schedule.schedule.dto.response.ScheduleDto;
import com.back.domain.schedule.schedule.dto.response.ScheduleWithClubDto;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.error.ScheduleErrorCode;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ClubRepository clubRepository;

    /**
     * 특정 모임의 일정 목록 조회
     * @param clubId
     * @return schedules
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto> getClubSchedules(Long clubId, LocalDate startDate, LocalDate endDate) {
        // dateTimeRange 생성
        DateTimeRange dateTimeRange = getDateTimeRange(startDate, endDate);
        LocalDateTime startDateTime = dateTimeRange.startDateTime();
        LocalDateTime endDateTime = dateTimeRange.endDateTime();

        // 활성화된 일정 중, 범위 내에 있는 목록을 시작 날짜 기준으로 오름차순 정렬하여 조회
        List<Schedule> schedule =  scheduleRepository
                .findSchedulesByClubAndDateRange(clubId, startDateTime, endDateTime);

        return schedule
                .stream()
                .map(ScheduleDto::new)
                .toList();
    }

    /**
     * 나의 모든 모임 일정 목록 조회 (월 단위)
     * @param memberId
     * @param startDate
     * @param endDate
     * @return
     */
    @Transactional(readOnly = true)
    public List<ScheduleWithClubDto> getMySchedules(Long memberId, LocalDate startDate, LocalDate endDate) {
        // dateTimeRange 생성
        DateTimeRange dateTimeRange = getDateTimeRange(startDate, endDate);
        LocalDateTime startDateTime = dateTimeRange.startDateTime();
        LocalDateTime endDateTime = dateTimeRange.endDateTime();

        // 나의 모든 모임 일정 목록을 월단위로 시작 날짜 기준으로 오름차순 정렬하여 조회
        List<Schedule> mySchedules = scheduleRepository
                .findMonthlySchedulesByMemberId(memberId, startDateTime, endDateTime);

        return mySchedules
                .stream()
                .map(ScheduleWithClubDto::new)
                .toList();
    }

    /**
     * 일정 엔티티 조회
     * @param scheduleId
     * @return schedule
     */
    @Transactional(readOnly = true)
    public Schedule getScheduleEntityById(Long scheduleId) {
        return scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException(ScheduleErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    /**
     * 일정 조회
     * @param scheduleId
     * @return scheduleDetailDto
     */
    @Transactional(readOnly = true)
    public ScheduleDetailDto getScheduleById(Long scheduleId) {
        return new ScheduleDetailDto(getScheduleEntityById(scheduleId));
    }

    /**
     * 활성화된 일정 엔티티 조회
     * @param scheduleId
     * @return Schedule
     */
    @Transactional(readOnly = true)
    public Schedule getActiveScheduleEntityById(Long scheduleId) {
        return scheduleRepository
                .findActiveScheduleById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException(ScheduleErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    /**
     * 활성화된 일정 조회
     * @param scheduleId
     * @return ScheduleDetailDto
     */
    @Transactional(readOnly = true)
    public ScheduleDetailDto getActiveScheduleById(Long scheduleId) {
        return new ScheduleDetailDto(getActiveScheduleEntityById(scheduleId));
    }

    /**
     * 특정 모임의 최신 일정 조회
     * @param clubId
     * @return schedule
     */
    @Transactional(readOnly = true)
    public ScheduleDetailDto getLatestClubSchedule(Long clubId) {
        Schedule schedule = scheduleRepository
                .findFirstByClubIdOrderByIdDesc(clubId)
                .orElseThrow(() -> new NoSuchElementException(ScheduleErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

        return new ScheduleDetailDto(schedule);
    }

    /**
     * 특정 모임의 일정 개수 조회
     * @param clubId
     * @return
     */
    @Transactional(readOnly = true)
    public long countClubSchedules(Long clubId) {
        return scheduleRepository.countByClubId(clubId);
    }

    /**
     * 일정 생성
     * @param reqBody (ScheduleCreateReqBody)
     * @return schedule
     */
    @Transactional
    public ScheduleDetailDto createSchedule(ScheduleCreateReqBody reqBody) {
        Club club = getClubOrThrow(reqBody.clubId());

        // 날짜 유효성 검증
        validateDate(reqBody.startDate(), reqBody.endDate());

        // 일정 생성
        Schedule schedule = Schedule.builder()
                .title(reqBody.title())
                .content(reqBody.content())
                .startDate(reqBody.startDate())
                .endDate(reqBody.endDate())
                .spot(reqBody.spot())
                .club(club)
                .build();
        scheduleRepository.save(schedule);

        return new ScheduleDetailDto(schedule);
    }

    /**
     * 일정 수정
     * @param schedule
     * @param reqBody
     */
    @Transactional
    public void modifySchedule(Schedule schedule, ScheduleUpdateReqBody reqBody) {
        // 날짜 유효성 검증
        validateDate(reqBody.startDate(), reqBody.endDate());

        // 일정 수정
        schedule.modify(reqBody.title(), reqBody.content(), reqBody.startDate(), reqBody.endDate(), reqBody.spot());
    }

    /**
     * 일정 삭제
     * @param schedule
     */
    @Transactional
    public void deleteSchedule(Schedule schedule) {
        if (schedule.canDelete()) {
            // 일정 삭제 - 체크리스트 없을 시
            scheduleRepository.delete(schedule);
        } else {
            // 일정 비활성화 - 체크리스트 있을 시
            schedule.deactivate();

            // 체크리스트 비활성화
            schedule.getCheckList().deactivate();
        }
    }

    /**
     * 날짜 유효성 검증
     * @param startDate
     * @param endDate
     */
    private static void validateDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ServiceException(400, "시작일은 종료일보다 이전이어야 합니다.");
        }
    }

    /**
     * 날짜 범위 생성
     * @param startDate
     * @param endDate
     * @return
     */
    private DateTimeRange getDateTimeRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (startDate != null && endDate != null) {
            // 시작일과 종료일이 모두 있는 경우, 해당 범위로 설정
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atStartOfDay();

            validateDate(startDateTime, endDateTime);
        } else if (startDate != null && endDate == null) {
            // 시작일만 있는 경우, 해당 달의 1일부터 마지막 날까지 범위 설정
            YearMonth month = YearMonth.from(startDate);

            startDateTime = startDate.atStartOfDay();
            endDateTime = month.plusMonths(1).atDay(1).atStartOfDay();

            validateDate(startDateTime, endDateTime);
        } else {
            // 날짜 파라미터 없는 경우, 현재 달을 기준으로 설정
            YearMonth currentMonth = YearMonth.now();
            startDateTime = currentMonth.atDay(1).atStartOfDay();
            endDateTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        }
        return new DateTimeRange(startDateTime, endDateTime);
    }

    /**
     * 모임 조회
     * @param clubId
     * @return club
     */
    private Club getClubOrThrow(Long clubId) {
        return clubRepository
                .findById(clubId)
                .orElseThrow(() -> new NoSuchElementException(ClubErrorCode.CLUB_NOT_FOUND.getMessage()));
    }
}
