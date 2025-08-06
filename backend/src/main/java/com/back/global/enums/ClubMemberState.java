package com.back.global.enums;

import com.back.global.exception.ServiceException;

public enum ClubMemberState {
    INVITED("초대됨"),
    JOINING("가입 중"),
    APPLYING("가입 신청"),
    WITHDRAWN("탈퇴");

    private final String description;
    ClubMemberState(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public static ClubMemberState fromString(String state) {
        for (ClubMemberState clubMemberState : ClubMemberState.values()) {
            if (clubMemberState.name().equalsIgnoreCase(state)) {
                return clubMemberState;
            }
        }
        throw new ServiceException(400, "Unknown Member state: " + state);
    }
}
