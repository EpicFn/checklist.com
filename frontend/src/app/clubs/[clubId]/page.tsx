'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { components } from "@/types/backend/apiV1/schema";
import LoadingSpinner from '@/components/global/LoadingSpinner';
import { getClubInfo } from '@/api/club';
import { getMyInfoInClub } from '@/api/myClub';
import ClubInfo from '@/components/domain/clubs/clubInfo';
import ClubInfoSideMenu from '@/components/domain/clubs/clubInfoSideMenu';

type ClubInfoResponse = components['schemas']['ClubInfoResponse'];
type MyInfoInClubResponse = components['schemas']['MyInfoInClub'];

export default function ClubPage() {
    const params = useParams();
    const clubId = params.clubId as string;
    const [isLoading, setIsLoading] = useState(true);
    const [clubInfo, setClubInfo] = useState<ClubInfoResponse | null>(null);
    const [myInfo, setMyInfo] = useState<MyInfoInClubResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    // 에러 메시지를 UI로 매핑
    const getErrorMessage = (error: string | null) => {
        switch (error) {
            case '404 : 해당 ID의 클럽을 찾을 수 없습니다.':
            case '404 : 클럽이 존재하지 않습니다.':
            case '404 : 해당 클럽은 비활성화 상태입니다.':
                return '해당 모임을 찾을 수 없습니다.';
            case '403 : 비공개 클럽 정보는 클럽 멤버만 조회할 수 있습니다.':
            case '404 : 클럽 멤버 정보가 존재하지 않습니다.':
            case '403 : 가입 신청 중인 모임입니다. 가입 승인이 필요합니다.':
                return '접근 권한이 없습니다.';
            default:
                return error ? `오류가 발생했습니다: ${error}` : null;
        }
    };

    // 데이터 가져오기
    useEffect(() => {
        const fetchData = async () => {
            try {
                // 순차 호출 (병렬도 가능)
                const myInfo = await getMyInfoInClub(clubId);
                const data = await getClubInfo(clubId);

                if (!data?.data) {
                    throw new Error('해당 ID의 클럽을 찾을 수 없습니다.');
                }

                // APPLYING 상태인 경우, 접근 금지
                if (myInfo.state === 'APPLYING') {
                    throw new Error('403 : 가입 신청 중인 모임입니다. 가입 승인이 필요합니다.');
                }

                // WITHDRAWN 상태인 경우, 접근 금지
                if (myInfo.state === 'WITHDRAWN') {
                    throw new Error('404 : 클럽 멤버 정보가 존재하지 않습니다.');
                }

                setClubInfo(data.data);
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
    const errorMessage = getErrorMessage(error);
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
            <section className="w-4/5" style={{ maxHeight: '100%', borderRadius: '8px' }}>
                {clubInfo ? <ClubInfo club={clubInfo} /> : <div className="text-center">클럽 정보를 불러오는 중입니다...</div>}
            </section>

        </div>
    );
}