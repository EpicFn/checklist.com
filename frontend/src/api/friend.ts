import type { components } from "@/types/backend/apiV1/schema";
import { FriendStatusDto } from '@/types/friend';

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL!;
if (!BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_BASE_URL이 정의되어 있어야 합니다.");
}

// 공통 처리용 함수
async function handleResponse<T>(res: Response): Promise<T> {
  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.message || "요청 처리 중 오류가 발생했습니다.");
  }
  return data;
}

// 친구 목록 조회
export async function getFriends(
  status?: FriendStatusDto
): Promise<components["schemas"]["RsDataListFriendDto"]> {
  const url = new URL(`${BASE_URL}/api/v1/members/me/friends`);
  // searchParams
  if (status) {
    url.searchParams.append('status', status);
  }

  const res = await fetch(url.toString(), {
    method: "GET",
    credentials: "include",
  });
  return handleResponse(res);
}

// 친구 요청
export async function addFriend(
  friend_email: string
): Promise<components["schemas"]["RsDataFriendDto"]> {
  const res = await fetch(`${BASE_URL}/api/v1/members/me/friends`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ friend_email }),
  });
  return handleResponse(res);
}

// 친구 수락
export async function acceptFriend(
  friendId: number
): Promise<components["schemas"]["RsDataFriendDto"]> {
  const res = await fetch(`${BASE_URL}/api/v1/members/me/friends/${friendId}/accept`, {
    method: "PATCH",
    credentials: "include",
  });

  return handleResponse(res);
}

// 친구 거절
export async function rejectFriend(
  friendId: number
): Promise<components["schemas"]["RsDataFriendDto"]> {
  const res = await fetch(`${BASE_URL}/api/v1/members/me/friends/${friendId}/reject`, {
    method: "PATCH",
    credentials: "include",
  });
  return handleResponse(res);
}

// 친구 삭제
export async function deleteFriend(
  friendId: number
): Promise<components["schemas"]["RsDataFriendMemberDto"]> {
  const res = await fetch(`${BASE_URL}/api/v1/members/me/friends/${friendId}`, {
    method: "DELETE",
    credentials: "include",
  });
  return handleResponse(res);
}