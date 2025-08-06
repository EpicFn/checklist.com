// src/services/club.ts

import { components } from '@/types/backend/apiV1/schema';

type ClubMembersResponse = components['schemas']['RsDataClubMemberResponse'];
type MemberInfo = components['schemas']['ClubMemberInfo'];
type ClubMemberRegisterRequest = components['schemas']['ClubMemberRegisterRequest'];
type ClubMemberRegisterInfo = components['schemas']['ClubMemberRegisterInfo'];

const API_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// 멤버 목록 조회
export const getClubMembers = async (clubId: string): Promise<MemberInfo[]> => {

    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members`, {
        method: 'GET',
        credentials: 'include', // 쿠키를 포함하여 요청
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '멤버 목록을 불러오는 데 실패했습니다.');
    }
    const result: ClubMembersResponse = await response.json();
    return result.data?.members || [];
};

// 가입 신청 승인
export const approveApplication = async (clubId: string, memberId: number) => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members/${memberId}/approval`, {
        method: 'PATCH',
        credentials: 'include',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '가입 승인에 실패했습니다.');
    }
};

// 가입 신청 거절
export const rejectApplication = async (clubId: string, memberId: number) => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members/${memberId}/approval`, {
        method: 'DELETE',
        credentials: 'include',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '가입 거절에 실패했습니다.');
    }
};

// 멤버 역할 변경
export const changeMemberRole = async (clubId: string, memberId: number, role: 'MANAGER' | 'PARTICIPANT') => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members/${memberId}/role`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ role }),
        credentials: 'include',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '멤버 역할 변경에 실패했습니다.');
    }
};

// 클럽에서 멤버 삭제
export const deleteMember = async (clubId: string, memberId: number) => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members/${memberId}`, {
        method: 'DELETE',
        credentials: 'include',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '멤버 삭제에 실패했습니다.');
    }
};

// 클럽에 멤버 초대
export const inviteMembers = async (clubId: string, emails: string[]) => {
    // emails 배열을 ClubMemberRegisterInfo 배열로 변환 (role은 'PARTICIPANT'로 고정)
    const members: ClubMemberRegisterInfo[] = emails.map(email => ({
        email,
        role: 'PARTICIPANT',
    }));

    // ClubMemberRegisterRequest 객체 생성
    const body: ClubMemberRegisterRequest = { members };

    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}/members`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
        credentials: 'include',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '멤버 추가에 실패했습니다.');
    }
};