'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { getClubInfoByInvitationToken, applyToClubByInvitationToken } from '@/api/clubLink';

// 클럽 데이터 인터페이스
interface ClubData {
  clubId: number;
  name: string;
  category: string;
  imageUrl: string;
  mainSpot: string;
  eventType: string;
  startDate: string;
  endDate: string;
  leaderId: number;
  leaderName: string;
}

// 인증 모달 컴포넌트
function AuthModal({ onClose, onLogin, onGuestProceed }: { onClose: () => void, onLogin: () => void, onGuestProceed: () => void }) {
  return (
    <div className="fixed inset-0 overflow-y-auto h-full w-full flex items-center justify-center z-50">
      <div className="bg-white p-8 rounded-lg shadow-xl max-w-sm w-full mx-4 text-center transform transition-all scale-100 opacity-100">
        <h2 className="text-2xl font-bold mb-4 text-gray-900">로그인이 필요합니다</h2>
        <p className="text-gray-600 mb-6">모임에 가입하려면 로그인이 필요합니다. 기존 계정으로 로그인하거나, 게스트 계정을 생성하여 진행할 수 있습니다.</p>
        <div className="space-y-4">
          <button
            onClick={onLogin}
            className="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-semibold shadow-md hover:bg-blue-700 transition-colors"
          >
            로그인하기
          </button>
          <button
            onClick={onGuestProceed}
            className="w-full px-4 py-3 bg-gray-200 text-gray-800 rounded-lg font-semibold shadow-md hover:bg-gray-300 transition-colors"
          >
            비회원으로 진행하기
          </button>
          <button
            onClick={onClose}
            className="w-full px-4 py-3 text-gray-500 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}

export default function InvitationPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const [clubInfo, setClubInfo] = useState<ClubData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isApplying, setIsApplying] = useState(false);
  const [applyResult, setApplyResult] = useState<string | null>(null);
  const [showLoginModal, setShowLoginModal] = useState(false);

  useEffect(() => {
    if (!token) {
      setError('초대 토큰이 유효하지 않습니다.');
      setIsLoading(false);
      return;
    }

    const fetchClubInfo = async () => {
      try {
        const info = await getClubInfoByInvitationToken(token);
        setClubInfo(info);
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError('클럽 정보를 가져오는 중 오류가 발생했습니다.');
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchClubInfo();
  }, [token]);

  const handleApplyToClub = async () => {
    if (!token) {
      setError('초대 토큰이 유효하지 않습니다.');
      return;
    }

    // 로그인 상태 확인
    const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    if (!accessToken) {
      setShowLoginModal(true);
      return;
    }

    setIsApplying(true);
    setApplyResult(null);
    try {
      // applyToClubByInvitationToken 함수는 `api/clubLink`에 정의되어 있어야 합니다.
      const message = await applyToClubByInvitationToken(token); 
      setApplyResult(message);
      alert('모임 가입 신청이 완료되었습니다!');
      router.push('/');
    } catch (err) {
      if (err instanceof Error) {
        // 오류 메시지를 알리고 메인 페이지로 이동
        alert(`가입 신청 중 오류가 발생했습니다: ${err.message}`);
        router.push('/');
      } else {
        alert('가입 신청 중 알 수 없는 오류가 발생했습니다.');
        router.push('/');
      }
    } finally {
      setIsApplying(false);
    }
  };
  
  const handleLogin = () => {
    router.push('/members/login');
  };

  const handleGuestProceed = () => {
    // 토큰이 올바르게 전달되도록 수정
    router.push(`/members/guest-register?token=${token}`);
    setShowLoginModal(false);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-xl font-semibold text-gray-700">클럽 정보를 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4">
        <p className="text-red-500 text-lg">{error}</p>
        <button
          onClick={() => router.push('/')}
          className="mt-4 px-6 py-2 bg-gray-200 text-gray-800 rounded-lg shadow-md hover:bg-gray-300 transition-colors"
        >
          홈으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className={`flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4 ${showLoginModal ? 'overflow-hidden' : ''}`}>
      {clubInfo && (
        <div className={`w-full max-w-xl bg-white p-8 rounded-xl shadow-2xl text-center transition-all duration-300 ${showLoginModal ? 'blur-sm pointer-events-none' : ''}`}>
          <h1 className="text-4xl font-extrabold text-gray-900 mb-6">{clubInfo.name}</h1>
          <p className="text-lg text-gray-600 mb-8">
            {clubInfo.leaderName}님이 초대하신 모임입니다.
          </p>
          <img
            src={clubInfo.imageUrl}
            alt={`${clubInfo.name} 이미지`}
            className="w-full h-64 object-cover rounded-xl mb-6 shadow-md"
          />
          <div className="space-y-4 text-left">
            <p className="text-gray-700">
              <span className="font-semibold">카테고리:</span> {clubInfo.category}
            </p>
            <p className="text-gray-700">
              <span className="font-semibold">장소:</span> {clubInfo.mainSpot}
            </p>
            <p className="text-gray-700">
              <span className="font-semibold">이벤트 타입:</span> {clubInfo.eventType}
            </p>
            <p className="text-gray-700">
              <span className="font-semibold">기간:</span> {clubInfo.startDate} ~ {clubInfo.endDate}
            </p>
          </div>
          {applyResult && (
              <p className="mt-4 text-green-600 font-semibold">{applyResult}</p>
          )}
          <button
            onClick={handleApplyToClub}
            className={`w-full mt-8 px-8 py-4 text-white font-semibold text-lg rounded-full shadow-lg transition-all duration-300 transform ${
                isApplying ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:scale-105 hover:bg-blue-700'
            }`}
            disabled={isApplying}
          >
            {isApplying ? '가입 신청 중...' : '가입 신청하기'}
          </button>
        </div>
      )}
      {showLoginModal && (
        <AuthModal
          onClose={() => setShowLoginModal(false)}
          onLogin={handleLogin}
          onGuestProceed={handleGuestProceed}
        />
      )}
    </div>
  );
}
