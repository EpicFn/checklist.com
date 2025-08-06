'use client';

import { useState, useEffect } from 'react';
import { components } from '@/types/backend/apiV1/schema'
import LoadingSpinner from '@/components/global/LoadingSpinner'
import { getMyClubs } from '@/api/myClub';
import ClubsManagement from '@/components/domain/clubManage/ClubsManagement';
import Link from 'next/link';

type MyClubList = components['schemas']['MyClubList'];
type ClubListItem = components['schemas']['ClubListItem'];

export default function MyClubsPage() {
    const [initialClubs, setInitialClubs] = useState<ClubListItem[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);


    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);
            try {
                // 순차 호출 (병렬도 가능)
                const initialClubs = await getMyClubs();

                if (!initialClubs) {
                    throw new Error('모임 목록을 불러오는 데 실패했습니다.');
                }
                setInitialClubs(initialClubs);
            } catch (err) {
                setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, []);

    // 가드 클로즈 : 로딩 중
    if (isLoading) return <LoadingSpinner />;

    // 가드 클로즈 : 에러 발생
    const errorMessage = error;
    if (errorMessage) {
        return <div className="text-center py-12">{errorMessage}</div>;
    }


    return (
        <div className="max-w-7xl mx-auto p-4">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-3xl font-bold">모임 관리</h1>
                <div className="flex gap-2">
                    <Link
                        href="/clubs/new"
                        className="px-4 py-2 rounded bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium"
                    >
                        모임 생성
                    </Link>
                    <Link
                        href="/members/mypage"
                        className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 text-gray-700 text-sm font-medium"
                    >
                        마이페이지
                    </Link>
                </div>
            </div>
            <section className="flex">
                <section className="w-full mb-4">
                    {/* 클라이언트 컴포넌트에 초기 데이터를 props로 전달 */}
                    <ClubsManagement initialClubs={initialClubs} />
                </section>
            </section>
        </div>
    );
}