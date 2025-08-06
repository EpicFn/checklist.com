'use client';

import * as React from 'react';
import { useState, useEffect, useCallback } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { fetchMe } from '@/api/members';

// ======================= API 함수 정의 =======================
// 사용자 정보 응답 데이터 타입
interface MeData {
    nickname: string;
    bio: string | null;
    profileImage: string | null;
    email: string;
}

// 프로필 업데이트 응답 데이터 타입
interface UpdateProfileResponse {
    message: string;
}

/**
 * 사용자 프로필 정보를 업데이트하는 함수입니다.
 * @param {object} params 업데이트할 정보.
 * @param {string} params.nickname 닉네임
 * @param {string | null} params.bio 자기소개
 * @param {string | undefined} params.newPassword 새 비밀번호
 * @param {File | null} params.profileImageFile 프로필 이미지 파일.
 * @returns {Promise<UpdateProfileResponse>} 업데이트 결과를 담은 객체를 반환합니다.
 */
const updateProfile = async ({
    nickname,
    bio,
    newPassword,
    profileImageFile
}: {
    nickname: string;
    bio: string | null;
    newPassword?: string;
    profileImageFile: File | null;
}): Promise<UpdateProfileResponse> => {
    const formData = new FormData();

    const profileData = {
        nickname,
        bio,
        ...(newPassword && { password: newPassword }),
    };

    formData.append('data', new Blob([JSON.stringify(profileData)], { type: 'application/json' }));

    if (profileImageFile) {
        formData.append('profileImage', profileImageFile);
    }

    const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/members/me`, {
        method: 'PUT',
        body: formData,
        credentials: 'include',
    });

    if (!response.ok) {
        let errorData = null;
        try {
            errorData = await response.json();
        } catch (e) {
            const errorText = await response.text();
            console.error('API Error Response (Text):', errorText);
            throw new Error(errorText || '프로필 업데이트에 실패했습니다.');
        }
        console.error('API Error Response (JSON):', errorData);
        throw new Error(errorData.message || '프로필 업데이트에 실패했습니다.');
    }

    const result: UpdateProfileResponse = await response.json();
    return result;
};


// ======================= 상수 및 컴포넌트 =======================
const DEFAULT_PROFILE_IMG = 'https://placehold.co/150x150/e5e7eb/6b7280?text=Profile';
const MAX_FILE_SIZE_MB = 5;

export default function EditProfilePage() {
    const router = useRouter();

    // 사용자 정보 상태 관리
    const [userInfo, setUserInfo] = useState({
        nickname: '',
        bio: '',
        profileImage: '',
        email: '',
    });

    // 비밀번호 변경 폼 상태
    const [newPassword, setNewPassword] = useState('');
    const [newPasswordConfirm, setNewPasswordConfirm] = useState('');
    // 프로필 이미지 파일 상태
    const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
    // 프로필 이미지 미리보기 URL 상태
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    // UI/API 상태
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [apiLoading, setApiLoading] = useState(false);

    // 사용자 정보를 불러오는 함수를 useCallback으로 메모이제이션
    const getUserInfo = useCallback(async () => {
        try {
            const meData = await fetchMe();

            setUserInfo({
                nickname: meData.nickname,
                bio: meData.bio || '',
                profileImage: meData.profileImage || '',
                email: meData.email,
            });
            setLoading(false);
        } catch (err: unknown) {
            console.error('API 호출 실패: 내 정보 불러오기', err);
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("사용자 정보를 불러오는 중 오류가 발생했습니다.");
            }
            setLoading(false);
        }
    }, []);

    // 컴포넌트 마운트 시 사용자 정보를 불러옵니다.
    useEffect(() => {
        getUserInfo();
    }, [getUserInfo]);

    // 프로필 이미지 파일이 변경될 때 미리보기 URL을 생성하고 해제합니다.
    useEffect(() => {
        if (profileImageFile) {
            const url = URL.createObjectURL(profileImageFile);
            setPreviewUrl(url);

            return () => {
                URL.revokeObjectURL(url);
            };
        } else {
            setPreviewUrl(null);
        }
    }, [profileImageFile]);

    // 파일 입력 핸들러
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            const maxSize = MAX_FILE_SIZE_MB * 1024 * 1024;

            // 파일 타입 검증
            if (!file.type.startsWith('image/')) {
                setError('이미지 파일만 업로드 가능합니다.');
                setProfileImageFile(null); // 잘못된 파일 상태 초기화
                e.target.value = ''; // input 필드 비우기
                return;
            }

            // 파일 크기 검증
            if (file.size > maxSize) {
                setError(`파일 크기는 ${MAX_FILE_SIZE_MB}MB 이하여야 합니다.`);
                setProfileImageFile(null); // 너무 큰 파일 상태 초기화
                e.target.value = ''; // input 필드 비우기
                return;
            }

            setProfileImageFile(file);
            setError(null);
        }
    };

    // 폼 제출 핸들러
    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setApiLoading(true);

        if (newPassword && newPassword !== newPasswordConfirm) {
            setError("새로운 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            setApiLoading(false);
            return;
        }

        // 제출 시 파일 크기 최종 검증
        const maxSize = MAX_FILE_SIZE_MB * 1024 * 1024;
        if (profileImageFile && profileImageFile.size > maxSize) {
            setError(`파일 크기는 ${MAX_FILE_SIZE_MB}MB 이하여야 합니다. 다시 선택해주세요.`);
            setApiLoading(false);
            return;
        }

        try {
            await updateProfile({
                nickname: userInfo.nickname,
                bio: userInfo.bio,
                newPassword,
                profileImageFile,
            });
            setSuccess("회원 정보가 성공적으로 수정되었습니다.");

            await getUserInfo();

            setNewPassword('');
            setNewPasswordConfirm('');
            setProfileImageFile(null);

            router.push('/members/mypage');

        } catch (err: unknown) {
            console.error('API 호출 실패: 회원 정보 수정', err);
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("서버와 통신 중 오류가 발생했습니다.");
            }
        } finally {
            setApiLoading(false);
        }
    }

    // 로딩 중 화면
    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-10 w-10 border-4 border-t-blue-500 border-gray-200"></div>
                <p className="ml-4 text-gray-600">사용자 정보를 불러오는 중...</p>
            </div>
        );
    }

    // 에러 발생 시 화면
    if (error && !apiLoading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-screen p-6 bg-gray-50 text-red-500">
                <p className="text-xl">오류가 발생했습니다: {error}</p>
                <button
                    onClick={() => router.back()}
                    className="mt-4 px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
                >
                    뒤로 가기
                </button>
            </div>
        );
    }

    // 메인 컴포넌트
    return (
        <main className="flex items-center justify-center min-h-screen p-6 bg-gray-50">
            <div className="w-full max-w-lg bg-white rounded-md shadow-md p-8">
                <h1 className="text-2xl font-bold mb-6 text-center">회원 정보 수정</h1>

                {success && <p className="mb-4 text-green-600 text-center">{success}</p>}
                {error && <p className="mb-4 text-red-600 text-center">{error}</p>}

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* 프로필 이미지 */}
                    <div className="flex flex-col items-center">
                        <Image
                            src={previewUrl || userInfo.profileImage || DEFAULT_PROFILE_IMG}
                            alt="Profile"
                            width={96}
                            height={96}
                            className="rounded-full object-cover mb-4"
                        />
                        <label htmlFor="profileImage" className="cursor-pointer text-blue-600 hover:underline">
                            프로필 이미지 변경
                        </label>
                        <input
                            id="profileImage"
                            name="profileImage"
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                            className="hidden"
                        />
                    </div>

                    {/* 닉네임 */}
                    <div>
                        <label htmlFor="nickname" className="block font-medium mb-1">닉네임</label>
                        <input
                            id="nickname"
                            name="nickname"
                            type="text"
                            value={userInfo.nickname}
                            onChange={e => setUserInfo({ ...userInfo, nickname: e.target.value })}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                            required
                        />
                    </div>

                    {/* 자기소개 */}
                    <div>
                        <label htmlFor="bio" className="block font-medium mb-1">자기소개</label>
                        <textarea
                            id="bio"
                            name="bio"
                            value={userInfo.bio}
                            onChange={e => setUserInfo({ ...userInfo, bio: e.target.value })}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                            rows={3}
                        />
                    </div>

                    {/* 새 비밀번호 */}
                    <div>
                        <label htmlFor="newPassword" className="block font-medium mb-1">새 비밀번호 (변경 시에만 입력)</label>
                        <input
                            id="newPassword"
                            name="newPassword"
                            type="password"
                            value={newPassword}
                            onChange={e => setNewPassword(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                            autoComplete="new-password"
                        />
                    </div>

                    {/* 새 비밀번호 확인 */}
                    <div>
                        <label htmlFor="newPasswordConfirm" className="block font-medium mb-1">새 비밀번호 확인</label>
                        <input
                            id="newPasswordConfirm"
                            name="newPasswordConfirm"
                            type="password"
                            value={newPasswordConfirm}
                            onChange={e => setNewPasswordConfirm(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                            autoComplete="new-password"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={apiLoading}
                        className={`w-full py-2 rounded text-white transition ${apiLoading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}`}
                    >
                        {apiLoading ? '수정 중...' : '변경 내용 저장'}
                    </button>
                </form>
            </div>
        </main>
    );
}