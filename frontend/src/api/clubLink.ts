// api/clubLink.ts

// 백엔드 API 응답 구조에 맞게 RsData 인터페이스를 정의합니다.
interface RsData<T> {
  code: number;
  message: string;
  data: T | null;
}

// 백엔드 응답에 맞게 ClubData 인터페이스를 정의합니다.
interface ClubData {
  clubId: number;
  name: string;
  category: string;
  imageUrl: string;
  mainSpot: string;
  eventType: string;
  startDate: string;
  endDate: string;
  leaderId: number;
  leaderName: string;
}

/**
 * 초대 토큰을 사용하여 클럽 정보를 불러오는 함수
 * @param inviteToken 클럽 초대 링크에 포함된 토큰
 * @returns 클럽 정보 데이터
 */
export async function getClubInfoByInvitationToken(inviteToken: string): Promise<ClubData> {
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/clubs/invitations/${inviteToken}`);
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '클럽 정보를 불러오는 데 실패했습니다.' }));
      throw new Error(errorData.message);
    }
    const result: RsData<ClubData> = await response.json();
    if (!result.data) {
      throw new Error('클럽 정보를 찾을 수 없습니다.');
    }
    return result.data;
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('알 수 없는 오류가 발생했습니다.');
  }
}

/**
 * 초대 토큰을 사용하여 비공개 클럽에 가입 신청하는 함수
 * @param inviteToken 클럽 초대 링크에 포함된 토큰
 * @returns 가입 신청 결과 메시지
 */
export async function applyToClubByInvitationToken(inviteToken: string): Promise<string> {
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
  const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

  if (!accessToken) {
    throw new Error('로그인이 필요합니다.');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/clubs/invitations/${inviteToken}/apply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // Authorization 헤더에 accessToken을 직접 추가합니다.
        'Authorization': `Bearer ${accessToken}`
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '가입 신청에 실패했습니다.' }));
      throw new Error(errorData.message);
    }

    const result: RsData<null> = await response.json();
    return result.message;

  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('알 수 없는 오류가 발생했습니다.');
  }
}
