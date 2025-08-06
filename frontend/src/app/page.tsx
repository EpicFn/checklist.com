// app/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { getPublicClubs } from '@/api/club';
import { components } from "@/types/backend/apiV1/schema";
import { useLogin } from './layout'; // 전역 상태 훅 가져오기

type SimpleClubInfoResponse = components['schemas']['SimpleClubInfoResponse'];

export default function MainPage() {
  const router = useRouter();
  const { isLoggedIn, showToast } = useLogin(); // 전역 상태와 함수 사용
  const [publicClubs, setPublicClubs] = useState<SimpleClubInfoResponse[]>([]);
  const [currentBannerIndex, setCurrentBannerIndex] = useState(0);
  const [isLoadingClubs, setIsLoadingClubs] = useState(true);

  useEffect(() => {
    const fetchPublicClubs = async () => {
      try {
        const response = await getPublicClubs();
        if (response.data && response.data.content) {
          setPublicClubs(response.data.content);
        }
      } catch (error) {
        console.error('Failed to fetch public clubs:', error);
        showToast('공개 모임 목록을 불러오는 데 실패했습니다.', 'error');
      } finally {
        setIsLoadingClubs(false);
      }
    };
    fetchPublicClubs();
  }, [showToast]); // showToast 의존성 추가

  useEffect(() => {
    if (publicClubs.length > 0) {
      const interval = setInterval(() => {
        setCurrentBannerIndex((prevIndex) => (prevIndex + 1) % publicClubs.length);
      }, 3000);
      return () => clearInterval(interval);
    }
  }, [publicClubs]);
  
  const goToBanner = (index: number) => {
    setCurrentBannerIndex(index);
  };

  const handleCreateClub = () => {
    if (isLoggedIn) {
      router.push('/clubs/new');
    } else {
      showToast('새 모임 만들기는 로그인 후 이용 가능합니다.', 'error', '/members/login');
    }
  };

  const getRandomGradient = () => {
    const colors = [
      '#FF6B6B', '#FFD166', '#06D6A0', '#118AB2', '#073B4C',
      '#A2D2FF', '#BDE0FE', '#CDB4DB', '#FFC8DD', '#FFAFCC',
      '#8338EC', '#3A86FF', '#FF006E', '#FB5607', '#FFBE0B'
    ];
    const getRandomColor = () => colors[Math.floor(Math.random() * colors.length)];
    
    let color1 = getRandomColor();
    let color2 = getRandomColor();
    while (color1 === color2) { 
      color2 = getRandomColor();
    }
    const direction = Math.random() > 0.5 ? 'to right' : 'to bottom right';
    return `linear-gradient(${direction}, ${color1}, ${color2})`;
  };

  return (
    <main className="flex-grow pt-24 pb-8">
      <div className="bg-gradient-to-r from-blue-500 to-indigo-600 text-white py-24 text-center">
        <div className="container mx-auto px-4">
          <h2 className="text-5xl md:text-6xl font-extrabold mb-4 animate-fadeIn">
            준비물 닷컴에 오신 것을 환영합니다!
          </h2>
          <p className="text-xl md:text-2xl opacity-80 max-w-2xl mx-auto">
            모임을 만들고, 필요한 준비물을 공유하고, 사람들을 초대해 보세요.
          </p>
        </div>
      </div>

      <div className="container mx-auto mt-16 px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-stretch">
          <div className="flex flex-col animate-fadeInLeft">
            <h3 className="text-4xl font-bold text-gray-900 mb-6 text-left md:ml-12">모임을 만들거나</h3>
            <div className="p-8 bg-white rounded-3xl shadow-xl w-full flex-grow flex flex-col justify-between transition-transform duration-300 hover:scale-[1.02] transform-gpu">
              <p className="text-lg text-gray-600 mb-6">
                새로운 모임을 만들고 멤버들을 초대하여 준비물을 관리해 보세요.
              </p>
              <div className="relative w-full h-64 rounded-2xl overflow-hidden mb-6">
                <Image
                  src="/create-club-image.jpg"
                  alt="새 모임 만들기 이미지"
                  layout="fill"
                  objectFit="cover"
                />
              </div>
              <button
                onClick={handleCreateClub}
                className="w-full px-8 py-4 bg-blue-600 text-white text-xl font-semibold rounded-full shadow-md transition-all duration-300 hover:bg-blue-700 hover:scale-105 focus:outline-none focus:ring-4 focus:ring-blue-400"
              >
                새 모임 만들기
              </button>
            </div>
          </div>

          <div className="flex flex-col animate-fadeInRight">
            <h3 className="text-4xl font-bold text-gray-900 mb-6 text-right md:mr-12">모임에 참여하세요</h3>
            <div className="p-8 bg-white rounded-3xl shadow-xl w-full flex-grow flex flex-col justify-between transition-transform duration-300 hover:scale-[1.02] transform-gpu">
              <p className="text-lg text-gray-600 mb-6">
                현재 활발하게 진행 중인 공개 모임들을 둘러보고 참여해보세요.
              </p>
              {isLoadingClubs ? (
                <div className="w-full h-64 bg-gray-200 rounded-2xl animate-pulse flex items-center justify-center">
                  <p className="text-xl text-gray-600">공개 모임을 불러오는 중...</p>
                </div>
              ) : publicClubs.length > 0 ? (
                <div className="relative w-full h-64 rounded-2xl shadow-lg overflow-hidden">
                  <div
                    className="absolute inset-0 flex transition-transform duration-500 ease-in-out"
                    style={{ transform: `translateX(-${currentBannerIndex * 100}%)` }}
                  >
                    {publicClubs.map((club, index) => (
                      <div
                        key={club.clubId}
                        className="flex-shrink-0 w-full h-full relative group"
                        onClick={() => router.push(`/clubs/${club.clubId}`)}
                      >
                        <div
                          className="absolute inset-0 bg-cover bg-center transition-all duration-500 group-hover:scale-105"
                          style={{
                            backgroundImage: club.imageUrl ? `url(${club.imageUrl})` : getRandomGradient(),
                          }}
                        ></div>
                        <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/30 transition-all duration-300 group-hover:bg-opacity-60">
                          <h2 className="text-4xl md:text-5xl font-extrabold text-white text-shadow-lg drop-shadow-md">
                            {club.name}
                          </h2>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2 z-10">
                    {publicClubs.map((_, index) => (
                      <button
                        key={index}
                        onClick={(e) => {
                          e.stopPropagation();
                          goToBanner(index);
                        }}
                        className={`w-3 h-3 rounded-full transition-colors duration-300 ${
                          currentBannerIndex === index ? 'bg-white' : 'bg-gray-400 hover:bg-white'
                        }`}
                      />
                    ))}
                  </div>
                </div>
              ) : (
                <div className="w-full h-64 bg-gray-100 rounded-2xl flex items-center justify-center text-center text-gray-500">
                  <p>현재 공개 모임이 없습니다.</p>
                </div>
              )}
              
              <button 
                onClick={() => router.push('/clubs/public')}
                className="w-full px-8 py-4 bg-blue-100 text-blue-600 text-xl font-semibold rounded-full shadow-md transition-all duration-300 hover:bg-blue-200 hover:scale-105 focus:outline-none focus:ring-4 focus:ring-blue-400 mt-6"
              >
                전체 공개 모임 보기
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}