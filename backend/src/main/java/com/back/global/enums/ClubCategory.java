package com.back.global.enums;

import com.back.global.exception.ServiceException;

public enum ClubCategory {
    STUDY("스터디"),
    HOBBY("취미"),
    SPORTS("운동"),
    TRAVEL("여행"),
    CULTURE("문화"),
    FOOD("음식"),
    PARTY("파티"),
    WORK("업무"),
    OTHER("기타");

    private final String description;

    ClubCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ClubCategory fromString(String category) {
        for (ClubCategory clubCategory : ClubCategory.values()) {
            if (clubCategory.name().equalsIgnoreCase(category)) {
                return clubCategory;
            }
        }
        throw new ServiceException(400, "Unknown Club category: " + category);
    }
}
