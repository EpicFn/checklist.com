package com.back.domain.schedule.schedule.checker;

import com.back.domain.club.club.checker.ClubAuthorizationChecker;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("scheduleAuthorizationChecker")
@RequiredArgsConstructor
public class ScheduleAuthorizationChecker {
    private final ScheduleService scheduleService;
    private final ClubAuthorizationChecker clubChecker;

    /**
     * 모임의 활성화된 스케줄에 대해 로그인한 유저가 모임 호스트인지 확인
     * @param scheduleId
     * @param memberId
     * @return 모임 호스트 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubHost(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleEntityById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isActiveClubHost(clubId, memberId);
    }

    /**
     * 모임의 활성화된 스케줄에 대해 로그인한 유저가 모임 관리자 또는 호스트인지 확인
     * @param scheduleId
     * @param memberId
     * @return 모임 관리자 또는 호스트 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubManagerOrHost(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleEntityById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isActiveClubManagerOrHost(clubId, memberId);
    }

    /**
     * 모임의 활성화된 스케줄에 대해 로그인한 유저가 모임 멤버인지 확인
     * @param scheduleId
     * @param memberId
     * @return 모임 멤버 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubMember(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleEntityById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isClubMember(clubId, memberId);
    }
}
