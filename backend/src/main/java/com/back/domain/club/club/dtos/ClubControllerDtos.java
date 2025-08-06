package com.back.domain.club.club.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

public class ClubControllerDtos {

    /**
     * 클럽 생성 요청을 위한 DTO 클래스입니다.
     * 클럽의 이름, 소개, 카테고리, 주요 장소, 최대 인원 수, 이벤트 유형, 시작일, 종료일,
     * 공개 여부, 리더 ID 및 클럽 멤버 정보를 포함합니다.
     */
    public static record CreateClubRequest(
        @NotBlank
        String name,
        @NotBlank
        String bio,
        @NotBlank
        String category,
        @NotBlank
        String mainSpot,
        @Min(value = 1)
        int maximumCapacity,
        @NotBlank
        String eventType,
        @NotBlank
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        String startDate,
        @NotBlank
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        String endDate,
        @NotNull
        Boolean isPublic,
        CreateClubRequestMemberInfo[] clubMembers
    ) {}

    public static record CreateClubRequestMemberInfo(
            @NotNull
            Long id,
            @NotBlank
            String role
    ){}

    /**
     * 클럽 수정 요청을 위한 DTO 클래스입니다.
     * 클럽의 이름, 소개, 카테고리, 주요 장소, 최대 인원 수, 이벤트 유형, 시작일, 종료일,
     * 공개 여부 및 리더 ID를 포함합니다.
     */
    public static record UpdateClubRequest(
            String name,
            String bio,
            String category,
            String mainSpot,
            Integer maximumCapacity,
            Boolean recruitingStatus,
            String eventType,
            String startDate,
            String endDate,
            Boolean isPublic
    ) {}

    /**
     * 클럽 생성 응답을 위한 DTO 클래스입니다.
     */
    public static record ClubResponse(
            Long clubId,
            Long leaderId
    ){}


    /**

     * 클럽 정보 조회 응답을 위한 DTO 클래스입니다.
     * 클럽의 ID, 이름, 소개, 카테고리, 주요 장소, 최대 인원 수, 이벤트 유형,
     * 시작일, 종료일, 공개 여부 및 리더 ID를 포함합니다.
     */
    public static record ClubInfoResponse(
            Long clubId,
            String name,
            String bio,
            String category,
            String mainSpot,
            int maximumCapacity,
            boolean recruitingStatus,
            String eventType,
            String startDate,
            String endDate,
            Boolean isPublic,
            String imageUrl,
            Long leaderId,
            String leaderName
    ) {
    }

    public static record SimpleClubInfoResponse(
            Long clubId,
            String name,
            String category,
            String imageUrl,
            String mainSpot,
            String eventType,
            String startDate,
            String endDate,
            Long leaderId,
            String leaderName
    ) {}

    public static record SimpleClubInfoWithoutLeader(
            Long clubId,
            String name,
            String category,
            String imageUrl,
            String mainSpot,
            String eventType,
            String startDate,
            String endDate,
            String bio
    ) {}
}
