package com.back.domain.club.clubLink.dtos;

public class ClubLinkDtos {

    /**

     * 클럽 초대 링크 응답을 위한 DTO 클래스입니다.
     * 클럽의 초대 링크를 포함합니다.
     */
    public static record CreateClubLinkResponse(
            String link
    ) {}
}
