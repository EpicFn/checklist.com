import React from 'react';
import { useParams, useRouter } from 'next/navigation';
import { COLORS } from '@/constants/colors';
import SettingButton from './settingButton';
import { leaveClub } from '@/api/myClub';

const menuItems = [
    { label: '정보', path: (clubId: string) => `/clubs/${clubId}` },
    { label: '멤버', path: (clubId: string) => `/clubs/${clubId}/members` },
    { label: '캘린더', path: (clubId: string) => `/schedule?clubId=${clubId}` }, 
    { label: '체크리스트', path: (clubId: string) => `/checklists?clubId=${clubId}` },
];

interface ClubInfoSideMenuProps {
    isHost: boolean;
}

const ClubInfoSideMenu: React.FC<ClubInfoSideMenuProps> = ({ isHost }) => {
    const params = useParams();
    const clubId = params.clubId as string;
    const router = useRouter();

    // 탈퇴 이벤트
    const handleLeaveClub = () => {
        if (confirm('정말로 클럽을 탈퇴하시겠습니까?')) {
            // 탈퇴 로직
            leaveClub(clubId)
                .then(() => {
                    alert('클럽을 성공적으로 탈퇴했습니다.');
                    router.push('/'); // 탈퇴 후 메인 페이지로 이동
                })
                .catch(err => {
                    alert(err instanceof Error ? err.message : '클럽 탈퇴에 실패했습니다.');
                });
        }
    };

    if (!clubId) return null;

    return (
        <nav
            style={{
                height: '100%',
                borderRadius: '12px',
                backgroundColor: COLORS.white,
                boxShadow: '0 8px 12px rgba(0, 0, 0, 0.13)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
            }}
        >
            <ul style={{
                listStyle: 'none',

                display: 'flex',
                flexDirection: 'column',
                gap: '12px',
                overflowY: 'auto',
                paddingTop: '16px',

            }}>
                {menuItems.map((item) => (
                    <li key={item.label} style={{ width: '100%' }}>
                        <button
                            style={{
                                background: COLORS.white,
                                color: '#222',
                                cursor: 'pointer',
                                fontSize: '1rem',
                                textAlign: 'left',
                                padding: '12px 16px',
                                width: '100%',
                                transition: 'background 0.2s, box-shadow 0.2s, color 0.2s',
                                fontWeight: '500',
                            }}
                            onClick={() => router.push(item.path(clubId))}
                            onMouseEnter={e => {
                                e.currentTarget.style.background = COLORS.brown;
                                e.currentTarget.style.color = COLORS.white;
                            }}
                            onMouseLeave={e => {
                                e.currentTarget.style.background = COLORS.white;
                                e.currentTarget.style.color = '#222';
                            }}
                        >
                            {item.label}
                        </button>
                    </li>
                ))}
            </ul>

            {/* footer */}
            <footer style={{ padding: '16px', textAlign: 'center', alignSelf: 'flex-start', display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                {
                    isHost && (
                        <SettingButton clubId={clubId} />
                    )
                }
                <button
                    style={{
                        background: '#e74c3c',
                        color: COLORS.white,
                        border: 'none',
                        borderRadius: '8px',
                        padding: '10px 20px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
                        transition: 'background 0.2s, color 0.2s',
                    }}
                    onMouseEnter={e => {
                        e.currentTarget.style.background = '#c0392b';
                    }}
                    onMouseLeave={e => {
                        e.currentTarget.style.background = '#e74c3c';
                    }}
                    onClick={handleLeaveClub}
                >
                    탈퇴
                </button>
            </footer>

        </nav>
    );
};

export default ClubInfoSideMenu;