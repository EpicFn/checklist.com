import type { components } from "@/types/backend/apiV1/schema";
import type { ScheduleDetailDto, RsDataScheduleDetailDto } from "@/types/schedule"; // 공용 타입

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

// 모임 일정 목록 조회
export async function getClubSchedules(
  clubId: number,
  query?: { startDate?: string; endDate?: string },
  signal?: AbortSignal
): Promise<components["schemas"]["RsDataListScheduleDto"]> {
  let url = `${BASE_URL}/api/v1/schedules/clubs/${clubId}`;
  // 쿼리 스트링 처리 (required = false)
  if (query) {
    const params = new URLSearchParams();
    if (query.startDate) params.append("startDate", query.startDate);
    if (query.endDate) params.append("endDate", query.endDate);
    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }
  }
  const res = await fetch(url, {
    method: "GET",
    signal,
    credentials: 'include',
  });
  return handleResponse<components["schemas"]["RsDataListScheduleDto"]>(res);
}


// 일정 조회
export async function getSchedule(
  scheduleId: number,
  signal?: AbortSignal
): Promise<RsDataScheduleDetailDto> {
  const res = await fetch(`${BASE_URL}/api/v1/schedules/${scheduleId}`, {
    signal,
    credentials: 'include',
  });
  return handleResponse(res);
}

// 일정 생성
export async function createSchedule(
  body: components["schemas"]["ScheduleCreateReqBody"]
): Promise<RsDataScheduleDetailDto> {
  const res = await fetch(`${BASE_URL}/api/v1/schedules`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: 'include',
    body: JSON.stringify(body),
  });
  return handleResponse(res);
}

// 일정 수정
export async function modifySchedule(
  scheduleId: number,
  body: components["schemas"]["ScheduleUpdateReqBody"]
): Promise<RsDataScheduleDetailDto> {
  const res = await fetch(`${BASE_URL}/api/v1/schedules/${scheduleId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: 'include',
    body: JSON.stringify(body),
  });
  return handleResponse(res);
}

// 일정 삭제
export async function deleteSchedule(
  scheduleId: number
): Promise<components["schemas"]["RsDataVoid"]> {
  const res = await fetch(`${BASE_URL}/api/v1/schedules/${scheduleId}`, {
    method: "DELETE",
    credentials: 'include',
  });
  return handleResponse(res);
}

// 나의 일정 목록 조회
export async function getMySchedules(
  query?: { startDate?: string; endDate?: string },
  signal?: AbortSignal
): Promise<components["schemas"]["RsDataListScheduleWithClubDto"]> {
  let url = `${BASE_URL}/api/v1/schedules/me`;
  if (query) {
    const params = new URLSearchParams();
    if (query.startDate) params.append("startDate", query.startDate);
    if (query.endDate) params.append("endDate", query.endDate);
    url += `?${params.toString()}`;
  }

  const res = await fetch(url, {
    method: "GET",
    signal,
    credentials: 'include',
  });
  return handleResponse<components["schemas"]["RsDataListScheduleWithClubDto"]>(res);
}

// 로그인한 사용자의 모임 정보(권한 확인용)
export async function getMyClubInfoForSchedule(
  clubId: number
): Promise<components["schemas"]["RsDataMyInfoInClub"]> {
  const url = `${BASE_URL}/api/v1/my-clubs/${clubId}`;
  
  const res = await fetch(url, {
    method: "GET",
    credentials: 'include',
  });
  
  return handleResponse<components["schemas"]["RsDataMyInfoInClub"]>(res);
}