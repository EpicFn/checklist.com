// src/components/domain/my-clubs/ClubList.tsx

import { components } from '@/types/backend/apiV1/schema';
import ClubCard from "./ClubCard";

type ClubListItem = components['schemas']['ClubListItem'];

interface ClubListProps {
    clubs: ClubListItem[];
    state?: 'JOINING' | 'APPLYING' | 'INVITED';
    onAcceptInvitation?: (clubId: number) => void;
    onRejectInvitation?: (clubId: number) => void;
    onCancelApplication?: (clubId: number) => void;
}

export default function ClubList({ clubs, state, onAcceptInvitation, onRejectInvitation, onCancelApplication }: ClubListProps) {
    // 1. 표시할 모임이 없는 경우
    if (clubs.length === 0) {
        return (
            <div className="text-center text-gray-500 py-20">
                <p>표시할 모임이 없습니다.</p>
            </div>
        );
    }

    // 2. 모임이 있는 경우, 그리드 레이아웃으로 ClubCard를 렌더링
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {clubs.map(club => (
                <ClubCard
                    key={club.clubId}
                    club={club}
                    onAcceptInvitation={onAcceptInvitation}
                    onRejectInvitation={onRejectInvitation}
                    onCancelApplication={onCancelApplication}
                />
            ))}
        </div>
    );
}