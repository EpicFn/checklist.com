'use client';

import MemberListItem from './MemberListItem';
import { components } from '@/types/backend/apiV1/schema';
import MultiEmailInput from './MultiEmailInput';
import { useParams } from 'next/navigation';

type MemberInfo = components['schemas']['ClubMemberInfo'];

// MemberListItem에서 필요한 함수들을 props로 내려받습니다.
interface MemberListProps {
    members: MemberInfo[];
    state?: 'JOINING' | 'APPLYING' | 'INVITED';
    isHost?: boolean;
    onApprove: (memberId: number) => void;
    onReject: (memberId: number) => void;
    onChangeRole: (memberId: number, role: 'MANAGER' | 'PARTICIPANT') => void;
    onDelete: (memberId: number) => void;
    onInvite: (emails: string[]) => void;
}

export default function MemberList({ members, state, isHost, onInvite, ...handlers }: MemberListProps) {
    const params = useParams();
    const clubId = params.clubId as string;

    return (
        <div>
            {state === 'INVITED' && isHost && (
                <div className="mb-4">
                    <MultiEmailInput
                        onSubmit={onInvite}
                        label="모임에 초대할 멤버의 이메일 주소를 입력하세요"
                        placeholder="예: user@example.com"
                    />
                </div>
            )}
            {members.length === 0 ? (
                <p className="text-center text-gray-500 py-10">해당하는 멤버가 없습니다.</p>
            ) : (
                <ul className="bg-white rounded-lg shadow">
                    {members.map(member => (
                        <MemberListItem key={member.clubMemberId} member={member} isHost={isHost} {...handlers} />
                    ))}
                </ul>
            )}
        </div>
    );
}