'use client';

import Link from 'next/link';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { components } from '@/types/backend/apiV1/schema';

type ClubListItem = components['schemas']['ClubListItem'];

interface ClubCardProps {
    club: ClubListItem;
    onAcceptInvitation?: (clubId: number) => void;
    onRejectInvitation?: (clubId: number) => void;
    onCancelApplication?: (clubId: number) => void;
}

export default function ClubCard({ club, onAcceptInvitation, onRejectInvitation, onCancelApplication }: ClubCardProps) {
    const router = useRouter();

    return (
        <div className="border rounded-lg shadow-md overflow-hidden">
            {club.myState === 'JOINING' ? (
                <Link href={`/clubs/${club.clubId}`}>
                    <div className="relative h-40 w-full">
                        <Image src={club.imageUrl || '/default-club-image.png'} alt={club.clubName!} fill style={{ objectFit: 'cover' }} />
                    </div>
                    <div className="p-4">
                        <h3 className="font-bold text-lg truncate">{club.clubName}</h3>
                        <p className="text-sm text-gray-600 truncate">{club.bio}</p>
                        <span className="text-xs bg-gray-200 px-2 py-1 rounded-full">{club.category}</span>
                        <span className="ml-2 text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
                            {club.myRole}
                        </span>
                    </div>
                </Link>
            ) : (
                <div>
                    <div className="relative h-40 w-full">
                        <Image src={club.imageUrl || '/default-club-image.png'} alt={club.clubName!} fill style={{ objectFit: 'cover' }} />
                    </div>
                    <div className="p-4">
                        <h3 className="font-bold text-lg truncate">{club.clubName}</h3>
                        <p className="text-sm text-gray-600 truncate">{club.bio}</p>
                        <span className="text-xs bg-gray-200 px-2 py-1 rounded-full">{club.category}</span>
                        <span className="ml-2 text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
                            {club.myRole}
                        </span>
                    </div>
                </div>
            )}
            {/* --- 조건부 버튼 렌더링 --- */}
            <div className="p-4 bg-gray-50 border-t flex justify-end space-x-2">
                {club.myState === 'INVITED' && (
                    <>
                        <button onClick={() => { onAcceptInvitation?.(club.clubId!) }} className="btn-primary text-sm">수락</button>
                        <button onClick={() => { onRejectInvitation?.(club.clubId!) }} className="btn-secondary text-sm">거절</button>
                    </>
                )}
                {club.myState === 'APPLYING' && (
                    <button onClick={() => { onCancelApplication?.(club.clubId!) }} className="btn-danger text-sm">신청 취소</button>
                )}
                {club.myState === 'JOINING' && (
                    <span className="text-sm text-green-600 font-semibold">가입 완료</span>
                )}
            </div>
        </div>
    );
}