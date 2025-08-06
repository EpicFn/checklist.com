// src/api/members.ts

import type { components } from "@/types/backend/apiV1/schema";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// API 호출 시 공통으로 사용할 fetcher 함수
export async function fetcher(url: string, options: RequestInit = {}) {
  const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
    credentials: 'include',
  });

  if (!response.ok) {
    let errorMessage = `API 호출 실패: ${response.status}`;
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      // JSON 파싱 실패 시 기본 메시지 사용
    }
    throw new Error(errorMessage);
  }

  return response.json();
}

// 회원가입 API
export async function signUp({ email, password, nickname, bio }: { email: string; password: string; nickname: string; bio: string }): Promise<{ accessToken: string; refreshToken: string }> {
  const payload = { email, password, nickname, bio };
  const response = await fetcher('/api/v1/members/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (response && response.data && response.data.accessToken && response.data.apikey) {
    return {
      accessToken: response.data.accessToken,
      refreshToken: response.data.apikey,
    };
  }
  throw new Error("회원가입 응답에 토큰 정보가 없습니다.");
}

// 로그인 API
export async function login({ email, password }: { email: string; password: string }): Promise<{ accessToken: string; refreshToken: string }> {
  const payload = { email, password };
  const response = await fetcher('/api/v1/members/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (response && response.data && response.data.accessToken && response.data.apikey) {
    return {
      accessToken: response.data.accessToken,
      refreshToken: response.data.apikey,
    };
  }
  throw new Error("로그인 응답에 토큰 정보가 없습니다.");
}

// 게스트 계정 등록 API
export async function registerGuest(dto: any): Promise<any> {
  return fetcher('/api/v1/members/auth/guest-register', {
    method: 'POST',
    body: JSON.stringify(dto),
  });
}

// 내 정보 불러오기
export async function fetchMe(): Promise<any> {
  const response = await fetcher('/api/v1/members/me');
  return response.data;
}

// 내가 가입한 모임 불러오기
export async function fetchMyClubs(): Promise<any> {
  const response = await fetcher('/api/v1/my-clubs');
  return response.data.clubs;
}

// 내 친구 목록 불러오기
export async function fetchMyFriends(): Promise<any> {
  return await fetcher('/api/v1/members/me/friends');
}

// 내가 만든 프리셋 목록 불러오기
export async function fetchMyPresets(): Promise<any> {
  const response = await fetcher('/api/v1/presets');
  return response.data;
}

// 비밀번호 확인 API
export async function verifyPassword({ email, password }: { email: string; password: string }): Promise<any> {
  const payload = { email, password };
  const response = await fetcher('/api/v1/members/auth/verify-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return response.data;
}

export interface Friend {
  friendId: number;
  friendMemberId: number;
  friendNickname: string;
  friendBio: string;
  friendProfileImageUrl: string;
  status: 'ACCEPTED' | 'APPLIED' | 'PENDING';
}