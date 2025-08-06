package com.back.domain.member.friend.entity;

public enum FriendStatus {
    PENDING("요청 중"),    // 요청 중
    ACCEPTED("수락됨"),   // 수락됨
    REJECTED("거절됨"); // 거절됨

    private final String description;
    FriendStatus(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
