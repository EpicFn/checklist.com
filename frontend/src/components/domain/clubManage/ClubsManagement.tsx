'use client';

import { useState, useMemo, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { components } from '@/types/backend/apiV1/schema';
import ClubStateTabs from './ClubStateTabs';
import ClubList from './ClubList';
import { getMyClubs, acceptClubInvitation, rejectClubInvitation, cancelClubApplication } from '@/api/myClub';


type ClubListItem = components['schemas']['ClubListItem'];

interface ClubsManagementProps {
    initialClubs: ClubListItem[];
}

export default function ClubsManagement({ initialClubs }: ClubsManagementProps) {
    const [clubs, setClubs] = useState(initialClubs);
    const searchParams = useSearchParams();
    const currentState = searchParams.get('state') || 'JOINING';

    // 탭이 변경될 때마다 서버 데이터를 다시 불러오고 싶다면 useEffect 사용
    useEffect(() => {
        setClubs(initialClubs);
    }, [initialClubs]);

    const refreshClubs = async () => {
        try {
            const updatedClubs = await getMyClubs();
            setClubs(updatedClubs);
        } catch (err) {
            console.error('모임 목록을 불러오는 데 실패했습니다.', err);
            throw new Error('모임 목록을 불러오는 데 실패했습니다.');
        }
    };

    const filteredClubs = useMemo(() => {
        return clubs.filter(club => club.myState === currentState);
    }, [clubs, currentState]);

    const handleAction = async (action: () => Promise<void>) => {
        try {
            await action();

            alert('작업이 완료되었습니다.');
            await refreshClubs();
        } catch (err) {
            alert(err instanceof Error ? err.message : '작업에 실패했습니다.');
        }
    };

    const handleAcceptInvitation = (clubId: number) => handleAction(() => acceptClubInvitation(clubId));
    const handleRejectInvitation = (clubId: number) => handleAction(() => rejectClubInvitation(clubId));
    const handleCancelApplication = (clubId: number) => handleAction(() => cancelClubApplication(clubId));

    return (
        <>
            <ClubStateTabs />
            <ClubList
                clubs={filteredClubs}
                state={currentState as 'JOINING' | 'APPLYING' | 'INVITED'}
                onAcceptInvitation={handleAcceptInvitation}
                onRejectInvitation={handleRejectInvitation}
                onCancelApplication={handleCancelApplication}
            />
        </>
    );
}