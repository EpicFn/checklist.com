package com.back.domain.schedule.schedule.repository;

import com.back.domain.schedule.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 특정 모임의 일정 목록을 시작 날짜 기준으로 오름차순 정렬하여 조회
    List<Schedule> findByClubIdOrderByStartDate(Long clubId);

    // 특정 모임의 활성화된 일정 중, 범위 내에 있는 목록을 시작 날짜 기준으로 오름차순 정렬하여 조회
    @Query("""
            SELECT s FROM Schedule s 
            WHERE s.club.id = :clubId 
            AND s.isActive = true 
            AND s.startDate < :endDateTime
            AND s.endDate >= :startDateTime
            ORDER BY s.startDate
            """)
    List<Schedule> findSchedulesByClubAndDateRange(
            @Param("clubId") Long clubId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 나의 모든 모임 일정 목록을 월단위로 시작 날짜 기준으로 오름차순 정렬하여 조회
    @Query("""
            SELECT s FROM Schedule s
            JOIN s.club c
            JOIN c.clubMembers cm
            WHERE cm.member.id = :memberId
            AND s.isActive = true
            AND s.startDate < :endDateTime
            AND s.endDate >= :startDateTime
            ORDER BY s.startDate
        """)
    List<Schedule> findMonthlySchedulesByMemberId(
            @Param("memberId") Long memberId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 활성화된 일정 ID로 조회
    @Query("""
            SELECT s FROM Schedule s
            JOIN FETCH s.club
            WHERE s.id = :scheduleId
            AND s.isActive = true
            """)
    Optional<Schedule> findActiveScheduleById(Long scheduleId);

    // 특정 모임의 최신 일정을 ID 기준으로 내림차순 정렬하여 조회
    Optional<Schedule> findFirstByClubIdOrderByIdDesc(Long clubId);

    // 특정 모임의 일정 개수 조회
    long countByClubId(Long clubId);
}
