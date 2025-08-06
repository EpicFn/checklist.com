import { components } from "@/types/backend/apiV1/schema";

const API_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

type MyInfoInClub = components['schemas']['MyInfoInClub'];
type SimpleClubInfo = components['schemas']['SimpleClubInfo'];

type MyClubList = components['schemas']['MyClubList'];
type ClubListItem = components['schemas']['ClubListItem'];

/**
 * 모임에서 내 정보 조회
 * @param clubId 모임 ID
 * @returns 내 정보
 */
export const getMyInfoInClub = async (clubId: string): Promise<MyInfoInClub> => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}`, {
        method: 'GET',
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    const data = await response.json();

    if (!response.ok) {
        if (!('code' in data) || !('message' in data)) {
            throw new Error('내 정보를 가져오는 데 실패했습니다.');
        }
        throw new Error(data.code + " : " + (data.message || '내 정보를 가져오는 데 실패했습니다.'));
    }

    return data.data;
}

/**
 * 모임 가입 신청
 * @param clubId 모임 ID
 * @returns 가입 신청 결과 (모임 iid, 모임명)
 */
export const applyToJoinClub = async (clubId: string): Promise<SimpleClubInfo> => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}/apply`, {
        method: 'POST',
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    const data = await response.json();

    if (!response.ok) {
        if (!('code' in data) || !('message' in data)) {
            throw new Error('모임 가입 신청에 실패했습니다.');
        }
        throw new Error(data.code + " : " + (data.message || '모임 가입 신청에 실패했습니다.'));
    }

    return data.data;
}

/**
 * 모임 탈퇴
 * @param clubId 모임 ID
 */
export const leaveClub = async (clubId: string): Promise<void> => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}/withdraw`, {
        method: 'DELETE',
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    if (!response.ok) {
        const data = await response.json();
        if (!('code' in data) || !('message' in data)) {
            throw new Error('모임 탈퇴에 실패했습니다.');
        }
        throw new Error(data.code + " : " + (data.message || '모임 탈퇴에 실패했습니다.'));
    }
}

// 내 모임 목록 전체 조회
export const getMyClubs = async (): Promise<ClubListItem[]> => {

    const response = await fetch(`${API_URL}/api/v1/my-clubs`, {
        method: 'GET',
        credentials: 'include', // 쿠키를 포함하여 요청
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '모임 목록을 불러오는 데 실패했습니다.');
    }
    const result: { data?: MyClubList } = await response.json();
    return result.data?.clubs || [];
};

// [클라이언트용] 초대 수락
export const acceptClubInvitation = async (clubId: number) => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}/join`, {
        method: 'PATCH',
        credentials: 'include',
    });
    if (!response.ok) throw new Error('초대 수락에 실패했습니다.');
};

// [클라이언트용] 초대 거절
export const rejectClubInvitation = async (clubId: number) => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}/invitation`, {
        method: 'DELETE',
        credentials: 'include',
    });
    if (!response.ok) throw new Error('초대 거절에 실패했습니다.');
};

// [클라이언트용] 가입 신청 취소
export const cancelClubApplication = async (clubId: number) => {
    const response = await fetch(`${API_URL}/api/v1/my-clubs/${clubId}/apply`, {
        method: 'DELETE',
        credentials: 'include',
    });
    if (!response.ok) throw new Error('가입 신청 취소에 실패했습니다.');
};