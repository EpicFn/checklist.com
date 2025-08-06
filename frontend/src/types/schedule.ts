import type { components } from "@/types/backend/apiV1/schema";

// 일정 상세 DTO
export type ScheduleDetailDto = components["schemas"]["ScheduleDetailDto"];
// 일정 상세 응답 DTO
export type RsDataScheduleDetailDto = components["schemas"]["RsDataScheduleDetailDto"];

// 일정 생성 DTO
export type ScheduleCreateReqBody = components["schemas"]["ScheduleCreateReqBody"];

// 일정 수정 DTO
export type ScheduleUpdateReqBody = components["schemas"]["ScheduleUpdateReqBody"];