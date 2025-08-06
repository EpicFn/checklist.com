package com.back.domain.club.clubMember.dtos;

import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;

import java.time.LocalDate;
import java.util.List;

public class MyClubControllerDtos {

    /**
     * 클럽 초대 수락 응답 DTO
     * 클럽 ID와 클럽 이름을 포함
     */
    public static record SimpleClubInfo(
            Long clubId,
            String clubName
    ) {
    }

    /**
     * 클럽 내 내 정보 조회 응답 DTO
     * 클럽 멤버 ID, 클럽 ID, 클럽 이름, 역할, 상태를 포함
     */
    public static record MyInfoInClub(
            Long clubMemberId,
            Long clubId,
            String clubName,
            ClubMemberRole role,
            ClubMemberState state
    ) {
    }

    public static record MyClubList(
            List<ClubListItem> clubs
    ) {}

    public static record ClubListItem(
            Long clubId,
            String clubName,
            String bio,
            ClubCategory category,
            String imageUrl,
            String mainSpot,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isPublic,
            ClubMemberRole myRole,
            ClubMemberState myState
    ) {
    }
}