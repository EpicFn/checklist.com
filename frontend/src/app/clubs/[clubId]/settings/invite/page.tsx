'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import toast from 'react-hot-toast';

// 백엔드 API 응답에 맞춰 인터페이스를 수정합니다.
interface CreateClubLinkResponse {
  link: string; // <-- 'invitationLink'를 'link'로 변경
}

/**
 * 클럽 초대 링크를 생성하는 API를 호출하는 함수.
 * @param clubId 클럽 ID
 * @param accessToken JWT 액세스 토큰
 * @returns 생성된 초대 링크 URL
 */
async function generateClubLink(clubId: string, accessToken: string): Promise<string> {
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/clubs/${clubId}/members/invitation-link`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '서버 응답 파싱 실패' }));
      throw new Error(errorData.message || `초대 링크 생성 실패: ${response.status} ${response.statusText}`);
    }

    // RsData 구조에서 data 필드를 추출하고, link 속성을 반환하도록 수정합니다.
    const { data }: { data: CreateClubLinkResponse } = await response.json();
    return data.link; // <-- 'invitationLink'를 'link'로 변경

  } catch (error) {
    if (error instanceof TypeError) {
      console.error('Fetch Error:', error);
      throw new Error('네트워크 요청에 실패했습니다. 백엔드 서버의 CORS 설정을 확인해주세요.');
    }
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('알 수 없는 오류가 발생했습니다.');
  }
}

export default function CreateLinkPage() {
  const router = useRouter();
  const params = useParams();
  const clubId = params?.clubId as string;

  const [invitationLink, setInvitationLink] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    if (!clubId) {
      setError('유효하지 않은 클럽 ID입니다.');
      return;
    }
    
    const userAuthToken = localStorage.getItem('accessToken');
    if (!userAuthToken) {
      toast.error('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
      setTimeout(() => {
        router.push('/members/login');
      }, 2000);
    } else {
      setIsLoggedIn(true);
    }
  }, [clubId, router]);

  const handleGenerateLink = async () => {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      toast.error('초대 링크 생성은 로그인 후 이용 가능합니다.');
      setTimeout(() => {
        router.push('/members/login');
      }, 2000);
      return;
    }

    if (!clubId) {
      setError('클럽 ID를 찾을 수 없습니다.');
      return;
    }

    setIsLoading(true);
    setError(null);
    setInvitationLink(null);

    try {
      const link = await generateClubLink(clubId, accessToken);
      setInvitationLink(link);
      toast.success('초대 링크가 성공적으로 생성되었습니다!');
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
        toast.error(`초대 링크 생성 실패: ${err.message}`);
      } else {
        setError('초대 링크 생성 중 오류가 발생했습니다.');
        toast.error('초대 링크 생성 중 알 수 없는 오류가 발생했습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const copyToClipboard = () => {
    if (invitationLink) {
      try {
        const tempInput = document.createElement('input');
        tempInput.value = invitationLink;
        document.body.appendChild(tempInput);
        tempInput.select();
        document.execCommand('copy');
        document.body.removeChild(tempInput);
        toast.success('초대 링크가 클립보드에 복사되었습니다.');
      } catch (err) {
        console.error('Failed to copy to clipboard:', err);
        toast.error('클립보드 복사에 실패했습니다.');
      }
    }
  };

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
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4">
      <div className="w-full max-w-xl bg-white p-8 rounded-xl shadow-2xl text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 mb-6">초대 링크 생성</h1>
        <p className="text-lg text-gray-600 mb-8">아래 버튼을 눌러 모임에 초대할 수 있는 링크를 만드세요.</p>

        {!invitationLink && (
          <button
            onClick={handleGenerateLink}
            disabled={isLoading || !isLoggedIn}
            className="w-full px-8 py-4 bg-blue-600 text-white font-semibold text-lg rounded-full shadow-lg transition-all duration-300 transform hover:scale-105 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? '생성 중...' : '초대 링크 생성'}
          </button>
        )}

        {invitationLink && (
          <div className="mt-8">
            <p className="text-xl font-semibold text-gray-800 mb-4">생성된 초대 링크:</p>
            <div className="flex items-center justify-center space-x-4 mb-6">
              <input
                type="text"
                value={invitationLink}
                readOnly
                className="flex-1 px-4 py-3 border border-gray-300 rounded-lg bg-gray-100 text-gray-700 text-sm md:text-base font-mono truncate"
              />
              <button
                onClick={copyToClipboard}
                className="px-6 py-3 bg-green-500 text-white rounded-lg font-medium shadow-md hover:bg-green-600 transition-colors"
              >
                복사
              </button>
            </div>
            <button
              onClick={() => setInvitationLink(null)}
              className="mt-4 px-6 py-3 bg-red-500 text-white rounded-lg font-medium shadow-md hover:bg-red-600 transition-colors"
            >
              다시 생성
            </button>
          </div>
        )}
      </div>
    </div>
  );
}