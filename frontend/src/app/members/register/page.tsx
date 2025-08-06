'use client';

import * as React from "react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { signUp } from '@/api/members'; // members.ts에서 함수를 import
import { useLogin } from '../../layout'; // 전역 상태 훅 가져오기

export default function SignUpPage() {
  const router = useRouter();
  const { showToast, setIsLoggedIn } = useLogin(); // 전역 상태와 함수 사용

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (password !== passwordConfirm) {
      setError("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      showToast("비밀번호와 비밀번호 확인이 일치하지 않습니다.", "error");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError("올바른 이메일 형식을 입력해주세요.");
      showToast("올바른 이메일 형식을 입력해주세요.", "error");
      return;
    }

    if (password.length < 8) {
      setError("비밀번호는 최소 8자 이상이어야 합니다.");
      showToast("비밀번호는 최소 8자 이상이어야 합니다.", "error");
      return;
    }

    if (!email || !password || !nickname) {
      setError("이메일, 비밀번호, 닉네임은 필수 입력 항목입니다.");
      showToast("이메일, 비밀번호, 닉네임은 필수 입력 항목입니다.", "error");
      return;
    }

    setLoading(true);

    try {
      // signUp 함수가 반환하는 객체에서 accessToken과 refreshToken(apikey)을 받습니다.
      const { accessToken, refreshToken } = await signUp({
        email: email,
        password: password,
        nickname: nickname,
        bio: bio,
      });

      console.log("회원가입 성공:", { accessToken, refreshToken });

      if (accessToken && refreshToken) {
        // accessToken과 refreshToken(apikey)을 모두 localStorage에 저장합니다.
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        
        setIsLoggedIn(true); // <---- 회원가입 성공 시 전역 상태 직접 업데이트
        showToast("회원가입에 성공했습니다!", "success");
        // 토큰 저장 후 메인 페이지로 이동
        router.push("/");
      } else {
        // 토큰이 없으면 오류를 표시하거나 로그인 페이지로 이동
        showToast("회원가입에 성공했으나, 토큰을 받지 못했습니다. 로그인 페이지로 이동합니다.", "error", "/members/login");
      }

    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
        showToast(`회원가입 실패: ${err.message}`, "error");
      } else {
        setError("서버와 통신 중 오류가 발생했습니다.");
        showToast("서버와 통신 중 오류가 발생했습니다.", "error");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex items-center justify-center min-h-screen p-6 bg-gray-50">
      <div className="w-full max-w-md bg-white rounded-md shadow-md p-6">
        <h1 className="text-2xl font-bold mb-6">회원가입</h1>

        {/* react-hot-toast를 사용하므로 이 부분은 주석 처리하거나 제거할 수 있습니다. */}
        {/* {error && <p className="mb-4 text-red-600">{error}</p>}
        {success && <p className="mb-4 text-green-600">{success}</p>} */}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block font-medium mb-1">이메일</label>
            <input
              id="email"
              name="email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              autoComplete="email"
            />
          </div>

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
              minLength={8}
              autoComplete="new-password"
            />
          </div>

          <div>
            <label htmlFor="passwordConfirm" className="block font-medium mb-1">비밀번호 확인</label>
            <input
              id="passwordConfirm"
              name="passwordConfirm"
              type="password"
              value={passwordConfirm}
              onChange={e => setPasswordConfirm(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              minLength={8}
              autoComplete="new-password"
            />
          </div>

          <div>
            <label htmlFor="nickname" className="block font-medium mb-1">닉네임</label>
            <input
              id="nickname"
              name="nickname"
              type="text"
              value={nickname}
              onChange={e => setNickname(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              autoComplete="nickname"
            />
          </div>

          <div>
            <label htmlFor="bio" className="block font-medium mb-1">자기소개</label>
            <textarea
              id="bio"
              name="bio"
              value={bio}
              onChange={e => setBio(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              rows={3}
              autoComplete="off"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2 rounded text-white transition ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'
            }`}
          >
            {loading ? '가입 중...' : '가입하기'}
          </button>
        </form>
      </div>
    </main>
  );
}