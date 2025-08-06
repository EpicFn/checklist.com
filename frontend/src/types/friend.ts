import type { components } from "@/types/backend/apiV1/schema";

// 친구
export type FriendDto = components["schemas"]["FriendDto"];
// 친구 정보
export type FriendMemberDto = components["schemas"]["FriendMemberDto"];

// 친구 상태
export enum FriendStatusDto {
    SENT = "SENT",
    RECEIVED = "RECEIVED",
    ACCEPTED = "ACCEPTED",
    REJECTED = "REJECTED",
    ALL = "ALL"
  }
  
export const FriendStatusMap: Record<FriendStatusDto, string> = {
  [FriendStatusDto.SENT]: "요청 중",
  [FriendStatusDto.RECEIVED]: "요청받음",
  [FriendStatusDto.ACCEPTED]: "수락됨",
  [FriendStatusDto.REJECTED]: "거절됨",
  [FriendStatusDto.ALL]: "전체",
};