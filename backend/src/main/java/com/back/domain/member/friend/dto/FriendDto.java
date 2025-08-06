package com.back.domain.member.friend.dto;

import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record FriendDto(
        @Schema(description = "친구 ID")
        Long friendId,
        @Schema(description = "친구(회원) ID")
        Long friendMemberId,
        @Schema(description = "친구(회원) 닉네임")
        String friendNickname,
        @Schema(description = "친구(회원) 자기소개")
        String friendBio,
        @Schema(description = "친구(회원) 프로필 이미지 URL")
        String friendProfileImageUrl,
        @Schema(description = "친구 관계")
        FriendStatus status
) {
    public FriendDto(Friend friend, Member friendMember) {
        this(
            friend.getId(),
            friendMember.getId(),
            friendMember.getNickname(),
            friendMember.getMemberInfo() != null ? friendMember.getMemberInfo().getBio() : null,
            friendMember.getMemberInfo() != null ? friendMember.getMemberInfo().getProfileImageUrl() : null,
            friend.getStatus()
        );
    }
}
