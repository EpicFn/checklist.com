import React from 'react';
import { useRouter } from 'next/navigation';
import { COLORS } from '@/constants/colors';
import Image from 'next/image';
import { useState, useRef, useEffect } from 'react';

interface SettingButtonProps {
    clubId: string
}

function SettingButton({ clubId }: SettingButtonProps) {
    const router = useRouter();
    const [open, setOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    const menuItems = [
        {
            label: '정보 수정',
            onClick: () => router.push(`/clubs/${clubId}/settings/edit`)
        },
        {
            label: '모임 삭제',
            onClick: () => router.push(`/clubs/${clubId}/settings/delete`)
        },
        {
            label: '초대 링크',
            onClick: () => router.push(`/clubs/${clubId}/settings/invite`)
        }
    ];

    // 메뉴 바깥 클릭 시 닫기
    useEffect(() => {
        if (!open) return;
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [open]);

    return (
        <div style={{ position: 'relative', marginTop: '16px', textAlign: 'center', display: 'inline-block' }} ref={menuRef}>
            <button
                style={{
                    background: 'none',
                    border: 'none',
                    padding: 0,
                    cursor: 'pointer',
                }}
                onClick={() => setOpen(prev => !prev)}
            >
                <Image
                    src="/icons/setting-gear.png"
                    alt="설정"
                    width={36}
                    height={36}
                    style={{ display: 'block' }}
                />
            </button>
            {open && (
                <div
                    style={{
                        position: 'absolute',
                        bottom: '44px',
                        left: '0%',
                        background: COLORS.brown,
                        borderRadius: '8px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.12)',
                        minWidth: '120px',
                        zIndex: 10,
                        padding: '8px 0'
                    }}
                >
                    {menuItems.map(item => (
                        <div
                            key={item.label}
                            onClick={() => {
                                setOpen(false);
                                item.onClick();
                            }}
                            style={{
                                color: COLORS.white,
                                padding: '10px 20px',
                                cursor: 'pointer',
                                fontWeight: 500,
                                fontSize: '1rem',
                                transition: 'background 0.2s',
                            }}
                            onMouseDown={e => e.preventDefault()}
                            onMouseOver={e => (e.currentTarget.style.background = COLORS.gray)}
                            onMouseOut={e => (e.currentTarget.style.background = 'transparent')}
                        >
                            {item.label}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default SettingButton;