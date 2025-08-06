'use client';

import React from 'react';
import { components } from "@/types/backend/apiV1/schema";
import ClubCard from '@/components/domain/clubs/clubCard';
import { getPublicClubs } from '@/api/club';
import { applyToJoinClub } from '@/api/myClub';
import { useEffect, useState } from 'react';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import { useSearchParams } from 'next/navigation';
import { useRouter } from 'next/navigation';
import { ClubCategory, ClubCategoryKorean } from '@/types/ClubCategory';
import { EventType, EventTypeKorean } from '@/types/EventType';
import ClubSearchBar from '@/components/domain/clubs/clubSearchBar';
import PagingUnit from '@/components/global/PagingUnit';
import Modal from '@/components/global/Modal';
import ClubIntroInfo from '@/components/domain/clubs/clubIntroInfo';


type SimpleClubInfoWithoutLeader = components['schemas']['SimpleClubInfoWithoutLeader'];
type ClubInfoResponse = components['schemas']['ClubInfoResponse'];

export default function ClubListPage() {
    const [clubs, setClubs] = useState<SimpleClubInfoWithoutLeader[]>([]);
    const [loading, setLoading] = useState(true);

    // 페이지네이션 상태
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    // 모달 상태
    const [selectedClub, setSelectedClub] = useState<SimpleClubInfoWithoutLeader | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    // 검색 파라미터
    const searchParams = useSearchParams();
    const router = useRouter();


    // 로컬 입력값 상태
    const [filters, setFilters] = useState<{
        name: string;
        mainSpot: string;
        clubCategory: ClubCategory | '';
        eventType: EventType | '';
    }>({
        name: searchParams.get('name') || '',
        mainSpot: searchParams.get('mainSpot') || '',
        clubCategory: (searchParams.get('clubCategory') as ClubCategory) || '',
        eventType: (searchParams.get('eventType') as EventType) || '',
    });

    // 검색어가 있는 경우, 해당 검색어로 필터링
    useEffect(() => {
        const fetchClubs = async () => {
            try {
                setLoading(true);

                const name = searchParams.get('name');
                const mainSpot = searchParams.get('mainSpot');
                const category = searchParams.get('clubCategory');
                const eventType = searchParams.get('eventType');

                const data = await getPublicClubs(
                    page, // page
                    10, // size
                    'id,desc', // sort
                    name,
                    category,
                    mainSpot,
                    eventType
                );

                setClubs(Array.isArray(data.data?.content) ? data.data.content : []);
                setTotalPages(data.data?.totalPages || 1);
            } catch (error) {
                console.error('모임 목록을 불러오는 중 오류 발생:', error);
                setClubs([]);
                setTotalPages(1);
                alert('모임 목록을 불러오는 중 오류가 발생했습니다. 나중에 다시 시도해주세요.');
            } finally {
                setLoading(false);
            }

        };

        fetchClubs();
    }, [
        page,
        searchParams.get('name'),
        searchParams.get('mainSpot'),
        searchParams.get('clubCategory'),
        searchParams.get('eventType')
    ]);


    const handleSubmit = () => {
        const params = new URLSearchParams();
        if (filters.name) params.set('name', filters.name);
        if (filters.mainSpot) params.set('mainSpot', filters.mainSpot);
        if (filters.clubCategory) params.set('clubCategory', filters.clubCategory);
        if (filters.eventType) params.set('eventType', filters.eventType);

        router.push(`/clubs/public?${params.toString()}`);
        setPage(0); // 페이지 초기화
    };

    const handleClubCardClick = (club: SimpleClubInfoWithoutLeader) => {
        setSelectedClub(club);
        setIsModalOpen(true);
    };

    const handleApplyButtonClick = async () => {
        if (!selectedClub) return;

        try {
            // 모임 가입 신청 API 호출
            const result = await applyToJoinClub(String(selectedClub.clubId));
            alert(`'${result.clubName}'에 가입 신청이 완료되었습니다.`);
            setIsModalOpen(false);
            setSelectedClub(null);
        } catch (error) {
            const errorMessage = (error as any)?.message || '알 수 없는 오류가 발생했습니다.';
            alert(`가입 신청에 실패했습니다\n ${errorMessage}`);
        }
    }


    // 가드 클로즈 : 로딩 중
    if (loading) {
        return <LoadingSpinner />;
    }

    return (
        <div className="max-w-5xl mx-auto p-4">
            {/* 검색 바 */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', marginBottom: 24 }}>
                <ClubSearchBar
                    value={filters}
                    onChange={(newFilters) => setFilters(newFilters)}
                    onSubmit={handleSubmit}
                />
            </div>

            {/* 모임 카드 리스트 */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
                {clubs.length > 0 ? (
                    clubs.map((club) => (
                        <div key={club.clubId} onClick={() => handleClubCardClick(club)}>
                            <ClubCard club={club} />
                        </div>
                    ))
                ) : (
                    <p>등록된 모임이 없습니다.</p>
                )}
            </div>

            {/* 페이지네이션 */}
            <PagingUnit
                page={page}
                totalPages={totalPages}
                setPage={setPage}
                className="mt-8"
            />

            {/* 모달 */}
            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
                {selectedClub && (
                    <div>
                        <div className="mt-4 text-sm text-gray-500" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <ClubIntroInfo club={selectedClub as ClubInfoResponse} />
                            <button
                                onClick={handleApplyButtonClick}
                                style={{
                                    padding: '0.7rem 1.5rem',
                                    background: 'linear-gradient(90deg, #6366f1 0%, #60a5fa 100%)',
                                    color: '#fff',
                                    border: 'none',
                                    borderRadius: '0.5rem',
                                    fontWeight: 600,
                                    fontSize: '1rem',
                                    cursor: 'pointer',
                                    transition: 'background 0.2s',
                                    width: 'fit-content',
                                    alignSelf: 'flex-end'
                                }}
                            >
                                가입 신청
                            </button>
                        </div>
                    </div>
                )}
            </Modal>
        </div>

    );
}