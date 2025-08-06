package com.back.global.enums;

import com.back.global.exception.ServiceException;

public enum EventType {
    // 일회성, 단기, 장기
    ONE_TIME("일회성"), // 한 번만 열리는 모임
    SHORT_TERM("단기"), // 특정 기간 동안 열리는 모임
    LONG_TERM("장기"); // 종료일 없이 지속적으로 열리는 모임

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static EventType fromString(String eventType) {
        for (EventType type : EventType.values()) {
            if (type.name().equalsIgnoreCase(eventType)) {
                return type;
            }
        }
        throw new ServiceException(400, "Unknown event type: " + eventType);
    }

}
