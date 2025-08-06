'use client';

import ClubDataForm, { ClubFormData } from '@/components/domain/clubs/clubDataForm';
import { createClub } from '@/api/club';
import { components } from "@/types/backend/apiV1/schema";
import { useRouter } from 'next/navigation';
import { useState } from 'react';

type CreateClubRequest = components['schemas']['CreateClubRequest'];
type ClubResponse = components['schemas']['ClubResponse'];

export default function NewClubPage() {
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();

    const handleSubmit = async (data: ClubFormData, image: File | null) => {
        //data를 schema 형태로 변환 (멤버 없음)
        const createClubRequest: CreateClubRequest = {
            name: data.name,
            bio: data.bio,
            category: data.category,
            mainSpot: data.mainSpot,
            maximumCapacity: data.maximumCapacity,
            eventType: data.eventType,
            startDate: data.activityPeriod.startDate,
            endDate: data.activityPeriod.endDate,
            isPublic: data.isPublic,
            clubMembers: []
        }


        // api 호출
        try {
            const clubResponse: ClubResponse = await createClub(createClubRequest, image);

            // 성공 시 모임 상세 페이지로 이동
            router.push(`/clubs/${clubResponse.clubId}`);
        }
        catch (error: any) {
            // 실패 시 에러 메시지 표시
            console.error('모임 생성 실패:', error);
            const errorMessage = error.message || '모임 생성에 실패했습니다. 다시 시도해주세요.';
            alert(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">모임 생성</h1>
            <ClubDataForm onSubmit={handleSubmit} isLoading={isLoading} />
        </div>
    );
}