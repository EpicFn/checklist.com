'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';

interface MemberTabsProps {
    clubId: string;
}

export default function MemberTabs({ clubId }: MemberTabsProps) {
    const searchParams = useSearchParams();
    const currentState = searchParams.get('state') || 'JOINING';

    const tabs = [
        { state: 'JOINING', label: '참여중인 멤버' },
        { state: 'APPLYING', label: '가입 신청' },
        { state: 'INVITED', label: '초대된 멤버' },
    ];

    return (
        <div className="mb-6 border-b">
            <nav className="flex space-x-4">
                {tabs.map(tab => (
                    <Link
                        key={tab.state}
                        href={`/clubs/${clubId}/members?state=${tab.state}`}
                        className={`py-2 px-4 font-medium ${currentState === tab.state ? 'border-b-2 border-blue-500 text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
                    >
                        {tab.label}
                    </Link>
                ))}
            </nav>
        </div>
    );
}