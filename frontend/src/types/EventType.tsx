export enum EventType {
    ONE_TIME = "ONE_TIME",
    SHORT_TERM = "SHORT_TERM",
    LONG_TERM = "LONG_TERM",
}

export const EventTypeKorean: Record<EventType, string> = {
    [EventType.ONE_TIME]: "일회성",
    [EventType.SHORT_TERM]: "단기",
    [EventType.LONG_TERM]: "장기"
};