'use client';

import * as React from "react";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { verifyPassword, fetchMe } from '@/api/members';

export default function PasswordVerificationPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [apiLoading, setApiLoading] = useState(false);

  useEffect(() => {
    const getEmail = async () => {
      try {
        const meData = await fetchMe();
        setEmail(meData.email);
        setLoading(false);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("사용자 정보를 불러오는 중 오류가 발생했습니다.");
        }
        setLoading(false);
      }
    };
    getEmail();
  }, []);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setApiLoading(true);

    try {
      const response = await verifyPassword({ email, password });
      
      if (response.verified) {
        console.log("비밀번호 인증 성공");
        router.push('/members/mypage/edit-profile'); 
      } else {
        // 백엔드에서 verified: false를 반환했을 경우
        setError("비밀번호가 올바르지 않습니다. 다시 시도해 주세요.");
      }
    } catch (err: unknown) {
      // API 호출 자체가 실패했을 경우
      if (err instanceof Error) {
        setError("비밀번호 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
      } else {
        setError("서버와 통신 중 알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setApiLoading(false);
    }
  }
  
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-gray-600">사용자 정보를 불러오는 중...</p>
      </div>
    );
  }

  return (
    <main className="flex items-center justify-center min-h-screen p-6 bg-gray-50">
      <div className="w-full max-w-md bg-white rounded-md shadow-md p-6">
        <h1 className="text-2xl font-bold mb-6 text-center">비밀번호 확인</h1>

        {error && <p className="mb-4 text-red-600 text-center">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <p className="text-sm text-gray-600 text-center">
            회원 정보를 안전하게 수정하려면 비밀번호를 다시 입력해 주세요.
          </p>
          
          <div>
            <label htmlFor="password" className="block font-medium mb-1">비밀번호</label>
            <input
              id="password"
              name="password"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            disabled={apiLoading}
            className={`w-full py-2 rounded text-white transition ${apiLoading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}`}
          >
            {apiLoading ? '확인 중...' : '확인'}
          </button>
        </form>
      </div>
    </main>
  );
}