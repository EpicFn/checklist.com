package com.back.domain.club.clubMember.dtos;

import com.back.global.enums.MemberType;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Objects;

public class ClubMemberDtos {

    /**
     * 클럽 멤버 정보를 등록하기 위한 DTO 클래스
     */
    public static record ClubMemberRegisterInfo(
            @NotBlank
            String email,
            @NotBlank
            String role
    ) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClubMemberRegisterInfo that = (ClubMemberRegisterInfo) o;
            return email.equals(that.email); // 이메일만 기준
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }
    }

    public static record ClubMemberRegisterRequest(
            @NotEmpty
            List<ClubMemberRegisterInfo> members
    ){}

    public static record ClubMemberRoleChangeRequest (
            @NotBlank
            String role
    ){}

    public static record ClubMemberInfo(
            Long clubMemberId,
            Long memberId,
            String nickname,
            String tag,
            ClubMemberRole role,
            String email,
            MemberType memberType,
            String profileImageUrl,
            ClubMemberState state
    ) {}

    public static record ClubMemberResponse(
            List<ClubMemberInfo> members
    ) {}
}
