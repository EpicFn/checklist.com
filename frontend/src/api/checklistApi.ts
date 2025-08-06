import { CheckListListResponse, CheckListResponse, CheckListUpdateReqDto, CheckListWriteReqDto, ApiErrorResponse } from '@/types/checklist';
import { components } from '@/types/backend/apiV1/schema';

export type ClubMemberResponse = components['schemas']['RsDataClubMemberResponse'];
export type ClubMember = components['schemas']['ClubMemberInfo'];

// 그룹 내 사용자 정보 타입
export interface GroupUserInfo {
  role: 'HOST' | 'MANAGER' | 'PARTICIPANT';
  state: 'JOINING' | 'PENDING' | 'INVITED';
}

export interface GroupUserResponse {
  code: number;
  message: string;
  data: GroupUserInfo;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export async function fetchChecklists(groupId: string): Promise<CheckListListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/checklists/group/${groupId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        case 404:
          throw new Error(`GROUP_NOT_FOUND:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function fetchChecklistDetail(checklistId: string): Promise<CheckListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/checklists/${checklistId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        case 404:
          throw new Error(`CHECKLIST_NOT_FOUND:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function updateChecklist(checklistId: string, updateData: CheckListUpdateReqDto): Promise<CheckListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/checklists/${checklistId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(updateData),
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        case 404:
          throw new Error(`CHECKLIST_NOT_FOUND:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function deleteChecklist(checklistId: string): Promise<void> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/checklists/${checklistId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    // 204 No Content 처리
    if (response.status === 204) {
      return;
    }
    const data = response.headers.get('content-length') !== '0' ? await response.json() : {};

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        case 404:
          throw new Error(`CHECKLIST_NOT_FOUND:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }
  } catch (error) {
    throw error;
  }
}

export async function fetchGroupMembers(clubId: string): Promise<ClubMemberResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/clubs/${clubId}/members?state=JOINING`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return data;
  } catch (error) {
    throw error;
  }
}

// 일정 정보 타입
export interface ScheduleInfo {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  clubId: number;
  checkListId?: number;
}

export interface ScheduleResponse {
  code: number;
  message: string;
  data: ScheduleInfo;
}

export async function fetchScheduleDetail(scheduleId: string): Promise<ScheduleResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/schedules/${scheduleId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 404:
          throw new Error(`SCHEDULE_NOT_FOUND:${message}`);
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function fetchGroupUserInfo(clubId: string): Promise<GroupUserResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/my-clubs/${clubId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      // 서버에서 code 필드를 제공하는 경우 해당 값을 우선 사용
      const statusCode = errorData?.code || response.status;
      
      switch (statusCode) {
        case 404:
          throw new Error(`GROUP_NOT_FOUND:${message}`);
        case 403:
          throw new Error(`ACCESS_DENIED:${message}`);
        default:
          throw new Error(`HTTP error! status: ${statusCode} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function createChecklist(checklistData: CheckListWriteReqDto): Promise<CheckListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/checklists`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(checklistData),
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    // 각 상태 코드별 구체적 에러 메시지
    if (!response.ok) {
      const errorData = data as ApiErrorResponse;
      const message = errorData?.message || '알 수 없는 오류가 발생했습니다.';
      
      switch (response.status) {
        case 404:
          throw new Error(`SCHEDULE_NOT_FOUND:${message}`);
        case 403:
          throw new Error(`PERMISSION_DENIED:${message}`);
        case 409:
          throw new Error(`CHECKLIST_ALREADY_EXISTS:${message}`);
        default:
          throw new Error(`HTTP error! status: ${response.status} - ${message}`);
      }
    }

    return data;
  } catch (error) {
    throw error;
  }
}