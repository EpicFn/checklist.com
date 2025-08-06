'use client';

import ClubDataForm, { ClubFormData } from '@/components/domain/clubs/clubDataForm';
import { updateClub, getClubInfo } from '@/api/club';
import { components } from "@/types/backend/apiV1/schema";
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import { useEffect } from 'react';
import { getMyInfoInClub } from '@/api/myClub';
import { useParams } from 'next/navigation';
import { ClubCategory, ClubCategoryKorean } from '@/types/ClubCategory';
import { EventType, EventTypeKorean } from '@/types/EventType';

type UpdateClubRequest = components['schemas']['UpdateClubRequest'];
type ClubResponse = components['schemas']['ClubResponse'];
type RsDataClubInfoResponse = components['schemas']['RsDataClubInfoResponse'];

export default function ModifyClubInfoPage() {
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();

    const [initData, setInitData] = useState<Partial<ClubFormData>>({})
    const [initImageUrl, setInitImageUrl] = useState<string | null>(null);
    const params = useParams();
    const clubId = params?.clubId as string;

    // 페이지가 로드될 때 모임 정보를 가져와서 초기 데이터로 설정
    useEffect(() => {
        const fetchClubData = async () => {
            setIsLoading(true);
            try {
                const [club, myInfo] = await Promise.all([
                    getClubInfo(clubId),
                    getMyInfoInClub(clubId)
                ]);

                // 클럽 정보가 없는 경우
                if (!club?.data) {
                    alert("잘못된 접근입니다.");
                    router.push("/");
                    return;
                }

                // 호스트 권한이 없는 경우
                if (myInfo.role !== 'HOST') {
                    alert("호스트만 모임을 수정할 수 있습니다.");
                    router.push(`/clubs/${clubId}`);
                    return;
                }

                const initial: Partial<ClubFormData> = {
                    name: club.data?.name,
                    bio: club.data?.bio,
                    category: club.data?.category as ClubCategory,
                    mainSpot: club.data?.mainSpot,
                    maximumCapacity: club.data?.maximumCapacity,
                    eventType: club.data?.eventType as EventType,
                    activityPeriod: {
                        startDate: club.data?.startDate || '',
                        endDate: club.data?.endDate || '',
                    },
                    isPublic: club.data?.isPublic || false,
                };
                setInitData(initial);

                // 이미지 URL이 있다면 초기 이미지 URL로 설정
                if (club.data?.imageUrl) {
                    console.log("이미지 URL:", club.data.imageUrl);
                    setInitImageUrl(club.data.imageUrl);
                }

                setIsLoading(false);
            } catch (error) {
                alert('잘못된 접근입니다.');
                router.push(`/`);
            }
        };

        fetchClubData();
    }, [clubId]);

    const handleSubmit = async (data: ClubFormData, image: File | null) => {
        //data를 schema 형태로 변환 (멤버 없음)
        const createClubRequest: UpdateClubRequest = {
            name: data.name,
            bio: data.bio,
            category: data.category,
            mainSpot: data.mainSpot,
            maximumCapacity: data.maximumCapacity,
            eventType: data.eventType,
            startDate: data.activityPeriod.startDate,
            endDate: data.activityPeriod.endDate,
            isPublic: data.isPublic
        }


        // api 호출
        try {
            const clubResponse: ClubResponse = await updateClub(clubId, createClubRequest, image);

            // 성공 시 모임 상세 페이지로 이동
            router.replace(`/clubs/${clubResponse.clubId}`);
            router.refresh();
            //router.push(`/clubs/${clubResponse.clubId}`);

        }
        catch (error: any) {
            // 실패 시 에러 메시지 표시
            console.error('모임 수정 실패:', error);
            const errorMessage = error.message || '모임 수정에 실패했습니다. 다시 시도해주세요.';
            alert(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    // 페이지 로딩 중에는 버튼 비활성화
    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <LoadingSpinner />
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto p-4">
            <div className="flex items-center justify-between mb-4">
                <h1 className="text-2xl font-bold mb-4">모임 수정</h1>
                <button
                    type="button"
                    className="mb-4 px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                    onClick={() => router.back()}
                >
                    ←
                </button>
            </div>
            <ClubDataForm
                onSubmit={handleSubmit}
                isLoading={isLoading}
                initialData={initData}
                initialImageUrl={initImageUrl}
            />
        </div>
    );
}