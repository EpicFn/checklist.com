'use client';

import Image from 'next/image';
import { components } from '@/types/backend/apiV1/schema';

type MemberInfo = components['schemas']['ClubMemberInfo'];

interface MemberListItemProps {
    member: MemberInfo;
    isHost?: boolean;
    onApprove: (memberId: number) => void;
    onReject: (memberId: number) => void;
    onChangeRole: (memberId: number, role: 'MANAGER' | 'PARTICIPANT') => void;
    onDelete: (memberId: number) => void;
}

export default function MemberListItem({ member, isHost, onApprove, onReject, onChangeRole, onDelete }: MemberListItemProps) {
    const memberId = member.memberId!;

    if (!memberId) {
        return <li className="text-red-500">멤버 ID가 없습니다.</li>;
    }

    const handleRoleChange = () => {
        const newRole = member.role === 'MANAGER' ? 'PARTICIPANT' : 'MANAGER';
        onChangeRole(memberId, newRole);
    };

    return (
        <li className="flex items-center justify-between p-4 border-b">
            <div className="flex items-center space-x-4">
                {member.profileImageUrl ? (
                    <Image
                        src={member.profileImageUrl}
                        alt={member.nickname || '프로필 이미지'}
                        width={40}
                        height={40}
                        className="rounded-full"
                    />
                ) : (
                    <span className="w-10 h-10 flex items-center justify-center rounded-full bg-gray-100">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="currentColor"
                            className="w-8 h-8 text-gray-500"
                        >
                            <path
                                fillRule="evenodd"
                                d="M7.5 6a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zM3.751 20.105a8.25 8.25 0 0116.498 0 .75.75 0 01-.75.75H4.501a.75.75 0 01-.75-.75z"
                                clipRule="evenodd"
                            />
                        </svg>
                    </span>
                )}
                <div>
                    <p className="font-bold">
                        {member.nickname} <span className="text-gray-500 font-normal">#{member.tag}</span>
                    </p>
                    <p className="text-sm text-gray-600">
                        {member.role} / {member.memberType}
                    </p>
                </div>
            </div>

            {/* 조건부 버튼 렌더링 */}
            {isHost && (
                <div className="flex space-x-2">
                    {member.state === 'APPLYING' && (
                        <>
                            <button onClick={() => onApprove(memberId)} className="btn-primary">수락</button>
                            <button onClick={() => onReject(memberId)} className="btn-secondary">거절</button>
                        </>
                    )}
                    {member.state === 'JOINING' && member.role !== 'HOST' && (
                        <>
                            <button onClick={handleRoleChange} >
                                {member.role === 'MANAGER' ? '참여자로 변경' : '매니저로 임명'}
                            </button>
                            <button onClick={() => onDelete(memberId)} className="btn-danger">삭제</button>
                        </>
                    )}
                </div>
            )}
        </li>
    );
}