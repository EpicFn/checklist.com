'use client';

import { getClubMembers } from '@/api/clubMember';
import { getMyInfoInClub } from '@/api/myClub';
import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { components } from '@/types/backend/apiV1/schema';
import MemberManagement from '@/components/domain/clubMembers/MemberManagement';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import ClubInfoSideMenu from '@/components/domain/clubs/clubInfoSideMenu';

type MemberInfo = components['schemas']['ClubMemberInfo'];

export default function MembersPage() {
    const params = useParams();
    const clubId = params.clubId as string;
    const [myInfo, setMyInfo] = useState<components['schemas']['MyInfoInClub'] | null>(null);
    const [initialMembers, setInitialMembers] = useState<MemberInfo[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);


    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);
            try {
                // 순차 호출 (병렬도 가능)
                const initialMembers = await getClubMembers(clubId);
                const myInfo = await getMyInfoInClub(clubId);

                if (!initialMembers) {
                    throw new Error('멤버 목록을 불러오는 데 실패했습니다.');
                }
                setInitialMembers(initialMembers);
                setMyInfo(myInfo);
            } catch (err) {
                setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [clubId]);

    // 가드 클로즈 : 로딩 중
    if (isLoading) return <LoadingSpinner />;

    // 가드 클로즈 : 에러 발생
    const errorMessage = error;
    if (errorMessage) {
        return <div className="text-center py-12">{errorMessage}</div>;
    }


    return (
        <div className="max-w-7xl mx-auto p-4 flex">
            <aside
                className="w-1/5 pr-4"
                style={{
                    position: 'sticky',
                    top: '1rem',
                    alignSelf: 'flex-start',
                    height: '80vh',
                    zIndex: 10,
                }}
            >
                <ClubInfoSideMenu isHost={myInfo?.role === 'HOST'} />
            </aside>
            <section className="w-4/5">
                {/* 클라이언트 컴포넌트에 초기 데이터를 props로 전달 */}
                <MemberManagement clubId={clubId} initialMembers={initialMembers} isHost={myInfo?.role === 'HOST'} />
            </section>

        </div>
    );
}