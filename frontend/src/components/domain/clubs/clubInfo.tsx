'use client';

import React from 'react';
import { components } from "@/types/backend/apiV1/schema";
import InfoCard from '@/components/global/InfoCard';
import { COLORS } from '@/constants/colors';
import { EventType, EventTypeKorean } from '@/types/EventType';
import { ClubCategory, ClubCategoryKorean } from '@/types/ClubCategory';
type ClubInfoResponse = components['schemas']['ClubInfoResponse'];

interface ClubInfoProps {
    club: ClubInfoResponse;
}

const ClubInfo: React.FC<ClubInfoProps> = ({ club }) => {
    return (
        <section style={{ padding: 16 }}>
            <header>
                <InfoCard title="모임명" content={club.name} color={COLORS.brown} contentColor="#FFFFFF" />
            </header>
            <div style={{ height: 16 }} />
            <main style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 32 }}>
                {/* Left: Name, Image, Description */}
                <section style={{ display: 'flex', alignItems: 'flex-start', gap: 16, flex: 1, flexDirection: 'column' }}>
                    <figure style={{ display: 'flex', alignItems: 'center', gap: 8, width: '100%' }}>
                        <img
                            src={club.imageUrl && club.imageUrl.trim() !== "" ? club.imageUrl : "/default-club-image.png"}
                            alt={club.name}
                            style={{ width: '100%', borderRadius: 8, objectFit: 'scale-down', background: '#ffffff', maxHeight: 500, height: 'auto' }}
                        />
                    </figure>
                </section>
                {/* Right: Meta Info */}
                <aside style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 8, minWidth: 220 }}>
                    <InfoCard title="지역" content={club.mainSpot} color={COLORS.white} contentColor={COLORS.brown} />
                    <InfoCard title="최대 인원" content={`${club.maximumCapacity}명`} color={COLORS.white} contentColor={COLORS.brown} />
                    <InfoCard title="카테고리" content={club.category && club.category in ClubCategoryKorean ? ClubCategoryKorean[club.category as ClubCategory] : '미정'} color={COLORS.white} contentColor={COLORS.brown} />
                    <InfoCard
                        title="시작일"
                        content={club.startDate ? club.startDate.replace(/-/g, '.') : '미정'}
                        color={COLORS.white}
                        contentColor={COLORS.brown}
                    />
                    <InfoCard
                        title="종료일"
                        content={club.endDate ? club.endDate.replace(/-/g, '.') : '미정'}
                        color={COLORS.white}
                        contentColor={COLORS.brown}
                    />
                    <InfoCard
                        title="이벤트 유형"
                        content={
                            club.eventType && club.eventType in EventTypeKorean
                                ? EventTypeKorean[club.eventType as EventType]
                                : '미정'
                        }
                        color={COLORS.white}
                        contentColor={COLORS.brown}
                    />
                    <InfoCard title="공개 여부" content={club.isPublic ? '공개' : '비공개'} color={COLORS.white} contentColor={COLORS.brown} />
                </aside>
            </main>
            <div style={{ height: 16 }} />
            <section>
                <InfoCard title="소개" content={club.bio} />
            </section>
        </section>
    );
};

export default ClubInfo;


