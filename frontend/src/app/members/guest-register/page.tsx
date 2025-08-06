'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { getClubInfoByInvitationToken } from '@/api/clubLink';
import { registerGuest } from '@/api/members';

// 클럽 데이터 인터페이스
interface ClubData {
  clubId: number;
  name: string;
}

export default function GuestRegisterPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const [clubId, setClubId] = useState<number | null>(null);
  const [clubName, setClubName] = useState<string | null>(null);
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!token) {
      alert('초대 토큰이 유효하지 않습니다.');
      // router.push('/'); // 페이지 이동을 막기 위해 주석 처리
      setIsLoading(false);
      return;
    }

    const fetchClubInfo = async () => {
      try {
        const info = await getClubInfoByInvitationToken(token);
        setClubId(info.clubId);
        setClubName(info.name);
      } catch (err) {
        let errorMessage = '클럽 정보를 불러오는 중 오류가 발생했습니다.';
        if (err instanceof Error) {
          errorMessage = err.message;
        }
        alert(errorMessage);
        // router.push('/'); // 페이지 이동을 막기 위해 주석 처리
      } finally {
        setIsLoading(false);
      }
    };

    fetchClubInfo();
  }, [token]); // router를 의존성 배열에서 제거

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // 입력값 유효성 검사
    if (!nickname || !password) {
      alert('닉네임과 비밀번호를 모두 입력해주세요.');
      return;
    }
    if (clubId === null) {
      alert('클럽 정보를 불러오지 못했습니다.');
      return;
    }

    setIsSubmitting(true);

    try {
      // registerGuest 함수는 clubId를 포함한 데이터를 전송해야 합니다.
      const response = await registerGuest({ nickname, password, clubId });
      
      // 액세스 토큰을 로컬 스토리지에 저장
      if (response.data?.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        alert('비회원 모임 가입이 완료되었습니다!');
        // router.push('/'); // 페이지 이동을 막기 위해 주석 처리
      } else {
        alert('액세스 토큰을 받지 못했습니다.');
        // router.push('/'); // 페이지 이동을 막기 위해 주석 처리
      }
    } catch (err) {
      let errorMessage = '게스트 등록 중 알 수 없는 오류가 발생했습니다.';
      if (err instanceof Error) {
        errorMessage = err.message;
      }
      alert(errorMessage);
      // router.push('/'); // 페이지 이동을 막기 위해 주석 처리
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-xl font-semibold text-gray-700">클럽 정보를 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4">
      <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-2xl text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 mb-2">비회원 가입</h1>
        <p className="text-lg text-gray-600 mb-6">'{clubName}' 모임에 가입하기</p>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-left text-gray-700 font-semibold mb-2" htmlFor="nickname">닉네임</label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition-colors"
              placeholder="닉네임을 입력하세요"
              required
            />
          </div>
          <div>
            <label className="block text-left text-gray-700 font-semibold mb-2" htmlFor="password">비밀번호</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition-colors"
              placeholder="비밀번호를 입력하세요"
              required
            />
          </div>
          
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full px-8 py-4 text-white font-semibold text-lg rounded-full shadow-lg transition-all duration-300 transform hover:scale-105 hover:bg-blue-700 bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? '가입 중...' : '가입하기'}
          </button>
        </form>
      </div>
    </div>
  );
}
