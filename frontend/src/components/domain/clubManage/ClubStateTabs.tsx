'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';

export default function ClubStateTabs() {
    // 1. useSearchParams 훅으로 현재 URL의 쿼리 파라미터에 접근
    const searchParams = useSearchParams();
    const currentState = searchParams.get('state') || 'JOINING'; // URL에 state가 없으면 'JOINING'을 기본값으로

    // 2. 탭 데이터 배열
    const tabs = [
        { state: 'JOINING', label: '가입한 모임' },
        { state: 'APPLYING', label: '신청한 모임' },
        { state: 'INVITED', label: '초대받은 모임' },
    ];

    return (
        <div className="mb-6 border-b">
            <nav className="-mb-px flex space-x-6">
                {tabs.map(tab => (
                    <Link
                        key={tab.state}
                        href={`/clubs-manage?state=${tab.state}`}
                        // 3. 현재 state와 탭의 state를 비교하여 조건부 스타일링
                        className={`py-3 px-1 font-medium text-sm whitespace-nowrap ${currentState === tab.state
                            ? 'border-b-2 border-blue-600 text-blue-600' // 활성 탭 스타일
                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300' // 비활성 탭 스타일
                            }`}
                    >
                        {tab.label}
                    </Link>
                ))}
            </nav>
        </div>
    );
}