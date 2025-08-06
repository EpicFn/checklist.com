'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Preset, PresetListResponse } from '@/types/preset';
import { fetchPresets } from '@/api/presetApi';
import PresetCard from '@/components/domain/preset/PresetCard';
import CreatePresetButton from '@/components/domain/preset/CreatePresetButton';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import ErrorMessage from '@/components/global/ErrorMessage';

// 데모 데이터
const DEMO_PRESETS: Preset[] = [
  {
    id: 1,
    name: '준비물 - 펜션ver',
    presetItems: [
      { id: 1, content: '삼겹살, 간식', category: 'PREPARATION', sequence: 1 },
      { id: 2, content: '세면 용품', category: 'PREPARATION', sequence: 2 },
      { id: 3, content: '잠옷', category: 'PREPARATION', sequence: 3 },
      { id: 4, content: '펜션 예약 확인', category: 'RESERVATION', sequence: 4 },
      { id: 5, content: '주차장 예약', category: 'RESERVATION', sequence: 5 },
    ],
  },
  {
    id: 2,
    name: '준비물 - 등산ver',
    presetItems: [
      { id: 6, content: '등산스틱', category: 'PREPARATION', sequence: 1 },
      { id: 7, content: '체인', category: 'PREPARATION', sequence: 2 },
      { id: 8, content: '물', category: 'PREPARATION', sequence: 3 },
      { id: 9, content: '등산로 확인', category: 'PRE_WORK', sequence: 4 },
      { id: 10, content: '날씨 체크', category: 'PRE_WORK', sequence: 5 },
      { id: 11, content: '보험 가입', category: 'ETC', sequence: 6 },
    ],
  },
  {
    id: 3,
    name: '예약 - 국내 ver',
    presetItems: [
      { id: 12, content: '펜션 예약하기', category: 'RESERVATION', sequence: 1 },
      { id: 13, content: '쏘카 빌리기', category: 'RESERVATION', sequence: 2 },
      { id: 14, content: '레스토랑 예약', category: 'RESERVATION', sequence: 3 },
      { id: 15, content: '여행 계획 세우기', category: 'PRE_WORK', sequence: 4 },
      { id: 16, content: '경로 확인', category: 'PRE_WORK', sequence: 5 },
    ],
  },
];

export default function PresetsPage() {
  const [presets, setPresets] = useState<Preset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoginRequired, setIsLoginRequired] = useState(false);
  const router = useRouter();

  const loadPresets = async () => {
    try {
      setLoading(true);
      setError(null);
      setIsLoginRequired(false);
      const response = await fetchPresets();
      setPresets(response.data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      // 401 에러인 경우 (로그인 필요)
      if (errorMessage.startsWith('LOGIN_REQUIRED:')) {
        setIsLoginRequired(true);
        setError('로그인이 필요합니다. 현재는 데모 화면을 보여드립니다.');
        // 데모 데이터 설정
        setPresets(DEMO_PRESETS);
      } else {
        setError('프리셋을 불러오는 중 오류가 발생했습니다.');
        setPresets([]);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPresets();
  }, []);

  const handlePresetClick = (preset: Preset) => {
    // 프리셋 상세 페이지로 이동
    router.push(`/presets/${preset.id}`);
  };

  const handleCreatePreset = () => {
    // 프리셋 생성 페이지로 이동
    router.push('/presets/create');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-4">
            <Link
              href="/"
              className="text-gray-600 hover:text-gray-800 transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">내 프리셋</h1>
          </div>
          <p className="text-gray-600">자주 사용하는 준비물과 예약 목록을 관리하세요</p>
        </div>



        {/* 생성 버튼 */}
        <div className="mb-8">
          <CreatePresetButton onClick={handleCreatePreset} />
        </div>

        {/* 프리셋 목록 */}
        {!presets || presets.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <svg className="w-24 h-24 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">아직 프리셋이 없습니다</h3>
            <p className="text-gray-600">새로운 프리셋을 만들어보세요!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {presets.map((preset) => (
              <PresetCard
                key={preset.id}
                preset={preset}
                onClick={handlePresetClick}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
} 