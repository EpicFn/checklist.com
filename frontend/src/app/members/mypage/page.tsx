'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  fetchMe,
  fetchMyClubs,
  fetchMyFriends,
  fetchMyPresets,
  Friend, // api/members.ts에서 수정된 Friend 인터페이스를 가져옵니다.
} from '@/api/members';

interface UserData {
  nickname: string;
  email: string;
  bio: string;
  profileImage: string | null;
}

interface Club {
  clubId: number;
  clubName: string;
  myRole: 'HOST' | 'MANAGER' | 'PARTICIPANT';
  myState: 'JOINING' | 'APPLIED';
}

interface Preset {
  id: number;
  name: string;
}

function MyPage() {
  const [userData, setUserData] = useState<UserData | null>(null);
  const [clubs, setClubs] = useState<Club[] | null>(null);
  const [friends, setFriends] = useState<Friend[] | null>(null);
  const [presets, setPresets] = useState<Preset[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [meData, clubsData, friendsResponse, presetsData] = await Promise.all([
          fetchMe(),
          fetchMyClubs(),
          fetchMyFriends(), // API 응답 전체를 받아옵니다.
          fetchMyPresets(),
        ]);

        setUserData(meData);
        setClubs(clubsData);
        
        // 친구 목록 데이터 처리 로직 수정
        // friendsResponse.data가 존재하고 배열인지 확인
        if (friendsResponse && Array.isArray(friendsResponse.data)) {
          // 'ACCEPTED' 상태인 친구만 필터링합니다.
          const acceptedFriends = friendsResponse.data.filter(
            (friend: Friend) => friend.status === 'ACCEPTED'
          );
          setFriends(acceptedFriends);
        } else {
          setFriends([]);
        }
        
        setPresets(presetsData);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
          console.error('Fetch error:', err.message);
          if (err.message.includes('401')) {
            router.push('/login');
          }
        } else {
          setError('알 수 없는 오류가 발생했습니다.');
          console.error('Fetch error:', err);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [router]);

  const getRoleBadgeClass = (role: 'HOST' | 'MANAGER' | 'PARTICIPANT') => {
    switch (role) {
      case 'HOST':
        return 'bg-red-100 text-red-800';
      case 'MANAGER':
        return 'bg-blue-100 text-blue-800';
      case 'PARTICIPANT':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <p className="text-xl">회원 정보를 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-screen text-red-500">
        <p className="text-xl">에러 발생: {error}</p>
        <button
          onClick={() => router.push('/members/login')}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
        >
          로그인 페이지로
        </button>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-2xl mx-auto bg-white shadow-xl rounded-2xl mt-10">
      <h1 className="text-4xl font-bold text-center mb-6 text-gray-800">
        마이페이지
      </h1>

      {userData ? (
        <div className="space-y-6">
          <div className="text-center">
            <p className="text-2xl font-bold text-gray-800">
              안녕하세요, {userData.nickname}님!
            </p>
            <p className="text-lg text-gray-600 mt-2">
              {userData.email}
            </p>
          </div>

          <div className="relative flex items-start space-x-6 p-6 border-t border-gray-200">
            <div className="w-24 h-24 rounded-full flex-shrink-0 overflow-hidden bg-gray-200 flex items-center justify-center">
              {userData.profileImage ? (
                <img
                  src={userData.profileImage}
                  alt="프로필 이미지"
                  className="w-full h-full object-cover"
                />
              ) : (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  className="w-16 h-16 text-gray-500"
                >
                  <path
                    fillRule="evenodd"
                    d="M7.5 6a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zM3.751 20.105a8.25 8.25 0 0116.498 0 .75.75 0 01-.75.75H4.501a.75.75 0 01-.75-.75z"
                    clipRule="evenodd"
                  />
                </svg>
              )}
            </div>
            
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-gray-800">{userData.nickname}</h2>
              <p className="text-gray-600 mt-2">{userData.bio}</p>
            </div>
            
            <button
              onClick={() => router.push('/members/mypage/verify-password')}
              className="absolute bottom-6 right-6 text-sm px-3 py-1.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors font-semibold"
            >
              수정
            </button>
          </div>
          
          <div className="p-6 border rounded-lg shadow-sm">
            <div className="flex justify-between items-center mb-2">
              <h3 className="text-xl font-semibold">친구 목록</h3>
              <button 
                onClick={() => router.push('/members/friend')}
                className="text-sm px-3 py-1.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors font-semibold"
              >
                전체보기
              </button>
            </div>
            {friends && friends.length > 0 ? (
              <div className="space-y-2">
                {friends.map(friend => (
                  <div
                    key={friend.friendId}
                    className="flex items-center bg-gray-50 p-4 rounded-lg shadow-sm"
                  >
                    <span className="text-lg font-medium text-gray-800">{friend.friendNickname}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">현재 친구가 없습니다.</p>
            )}
          </div>

          <div className="p-6 border rounded-lg shadow-sm">
            <div className="flex justify-between items-center mb-2">
              <h3 className="text-xl font-semibold">가입한 모임 목록</h3>
              <button
                onClick={() => router.push('/clubs-manage')}
                className="text-sm px-3 py-1.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors font-semibold"
              >
                전체보기
              </button>
            </div>
            {clubs && clubs.length > 0 ? (
              <div className="space-y-2">
                {clubs
                  .filter(club => club.myState === 'JOINING')
                  .map(club => (
                  <div
                    key={club.clubId}
                    onClick={() => router.push(`/clubs/${club.clubId}`)}
                    className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition-colors cursor-pointer"
                  >
                    <span className="text-lg font-medium text-gray-800">{club.clubName}</span>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-semibold ${getRoleBadgeClass(club.myRole)}`}
                    >
                      {club.myRole === 'HOST' ? '모임장' : club.myRole === 'MANAGER' ? '매니저' : '참여자'}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">현재 가입한 모임이 없습니다.</p>
            )}
          </div>

          <div className="p-6 border rounded-lg shadow-sm">
            <div className="flex justify-between items-center mb-2">
              <h3 className="text-xl font-semibold">내가 만든 프리셋 목록</h3>
              <button
                onClick={() => router.push('/presets')}
                className="text-sm px-3 py-1.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors font-semibold"
              >
                전체보기
              </button>
            </div>
            {presets && presets.length > 0 ? (
              <div className="space-y-2">
                {presets.map(preset => (
                  <div
                    key={preset.id}
                    onClick={() => router.push(`/presets/${preset.id}`)}
                    className="flex items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition-colors cursor-pointer"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className="text-gray-500 mr-3"
                    >
                      <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.08a2 2 0 0 1 1 1.73v.55a2 2 0 0 1-1 1.73l-.15.08a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.73v-.55a2 2 0 0 1 1-1.73l.15-.08a2 2 0 0 0 .73-2.73l-.22-.39a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                    <span className="text-lg font-medium text-gray-800">{preset.name}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">현재 만든 프리셋이 없습니다.</p>
            )}
          </div>
        </div>
      ) : (
        <div className="text-center text-red-500">
          <p>회원 정보를 불러올 수 없습니다. 다시 로그인해 주세요.</p>
        </div>
      )}
    </div>
  );
}

export default MyPage;