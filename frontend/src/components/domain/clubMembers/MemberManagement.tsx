'use client';

import { useState, useMemo, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { components } from '@/types/backend/apiV1/schema';
import { getClubMembers } from '@/api/clubMember';
import MemberTabs from './MemberTabs';
import MemberList from './MemberList';
import { approveApplication, rejectApplication, changeMemberRole, deleteMember, inviteMembers } from '@/api/clubMember';

type MemberInfo = components['schemas']['ClubMemberInfo'];

interface MemberManagementProps {
    clubId: string;
    initialMembers: MemberInfo[];
    isHost: boolean;
}

export default function MemberManagement({ clubId, initialMembers, isHost }: MemberManagementProps) {
    const [members, setMembers] = useState(initialMembers);
    const searchParams = useSearchParams();
    const currentState = searchParams.get('state') || 'JOINING';

    // 탭이 변경될 때마다 서버 데이터를 다시 불러오고 싶다면 useEffect 사용
    useEffect(() => {
        setMembers(initialMembers);
    }, [initialMembers]);

    const refreshMembers = async () => {
        try {
            const updatedMembers = await getClubMembers(clubId);
            setMembers(updatedMembers);
        } catch (err) {
            console.error('멤버 목록을 불러오는 데 실패했습니다.', err);
            throw new Error('멤버 목록을 불러오는 데 실패했습니다.');
        }
    };

    const filteredMembers = useMemo(() => {
        return members.filter(member => member.state === currentState);
    }, [members, currentState]);

    const handleAction = async (action: () => Promise<void>) => {
        try {
            await action();

            alert('작업이 완료되었습니다.');
            await refreshMembers();
        } catch (err) {
            alert(err instanceof Error ? err.message : '작업에 실패했습니다.');
        }
    };

    const handleApprove = (memberId: number) => handleAction(() => approveApplication(clubId, memberId));
    const handleReject = (memberId: number) => handleAction(() => rejectApplication(clubId, memberId));
    const handleChangeRole = (memberId: number, role: 'MANAGER' | 'PARTICIPANT') => handleAction(() => changeMemberRole(clubId, memberId, role));
    const handleDelete = (memberId: number) => handleAction(() => deleteMember(clubId, memberId));
    const handleInviteMembers = (emails: string[]) => handleAction(() => inviteMembers(clubId, emails));

    return (
        <>
            <MemberTabs clubId={clubId} />
            <MemberList
                members={filteredMembers}
                state={currentState as 'JOINING' | 'APPLYING' | 'INVITED'}
                isHost={isHost}
                onApprove={handleApprove}
                onReject={handleReject}
                onChangeRole={handleChangeRole}
                onDelete={handleDelete}
                onInvite={handleInviteMembers}
            />
        </>
    );
}