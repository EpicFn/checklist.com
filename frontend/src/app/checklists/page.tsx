'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { CheckList } from '@/types/checklist';
import { fetchChecklists, fetchGroupUserInfo, GroupUserInfo } from '@/api/checklistApi';
import LoadingSpinner from '@/components/global/LoadingSpinner';


export default function SchedulesPage() {
  const [checklists, setChecklists] = useState<CheckList[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userInfo, setUserInfo] = useState<GroupUserInfo | null>(null);
  const [userInfoLoading, setUserInfoLoading] = useState(true);
  const router = useRouter();
  const searchParams = useSearchParams();
  const clubId = searchParams.get('clubId');

  const loadUserInfo = async () => {
    if (!clubId) {
      setUserInfoLoading(false);
      return;
    }
    
    try {
      setUserInfoLoading(true);
      const response = await fetchGroupUserInfo(clubId);
      setUserInfo(response.data);
    } catch (err) {
      // 사용자 정보를 불러올 수 없는 경우 null로 설정
      setUserInfo(null);
    } finally {
      setUserInfoLoading(false);
    }
  };

  const loadChecklists = async () => {
    if (!clubId) {
      setLoading(false);
      setError('클럽 ID가 필요합니다.');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetchChecklists(clubId);
      const checklistsData = response.data || [];
      
      // 서버 데이터를 클라이언트 형식으로 변환
      const convertedChecklists = checklistsData.map(checklist => ({
        ...checklist,
        checkListItems: checklist.checkListItems?.map(item => ({
          ...item,
          itemAssigns: item.itemAssigns?.map(assign => ({
            ...assign,
            id: assign.clubMemberId || assign.id, // clubMemberId를 id로 사용
          })) || []
        })) || []
      }));
      
      setChecklists(convertedChecklists);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      if (errorMessage.startsWith('ACCESS_DENIED:')) {
        setError('이 그룹의 체크리스트에 접근할 권한이 없습니다. 그룹 관리자에게 문의하세요.');
        setChecklists([]);
      } else {
        setError('체크리스트를 불러오는 중 오류가 발생했습니다.');
        setChecklists([]);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserInfo();
    loadChecklists();
  }, [clubId]);



  const handleChecklistClick = (checklist: CheckList) => {
    if (checklist.isActive) {
      if (!clubId) {
        alert('클럽 ID가 필요합니다. 올바른 클럽 페이지에서 접근해주세요.');
        return;
      }
      // 활성 체크리스트인 경우 상세보기로 이동
      router.push(`/checklists/${checklist.id}?clubId=${clubId}`);
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
    });
  };

  const isPastChecklist = (checklist: CheckList) => {
    if (!checklist.schedule?.endDate) return false;
    const now = new Date();
    const scheduleEnd = new Date(checklist.schedule.endDate);
    return scheduleEnd < now;
  };

  const formatDateRange = (startDate: string, endDate: string) => {
    if (startDate === endDate) {
      return formatDate(startDate);
    }
    return `${formatDate(startDate)} - ${formatDate(endDate)}`;
  };

  if (loading || userInfoLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="bg-white rounded-lg border border-red-200 shadow-sm p-8 text-center">
            <div className="text-red-500 mb-4">
              <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">오류가 발생했습니다</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <div className="flex gap-3 justify-center">
              <Link
                href="/"
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                홈으로 이동
              </Link>
              <button
                onClick={() => window.history.back()}
                className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                이전 페이지로
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <Link
                href="/"
                className="text-gray-600 hover:text-gray-800 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </Link>
              <h1 className="text-3xl font-bold text-gray-900">체크리스트 목록</h1>
            </div>
            
          </div>
          <p className="text-gray-600">체크리스트를 관리하고 할 일들을 체크하세요</p>
        </div>


        {/* 체크리스트 목록 */}
        {checklists.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <svg className="w-24 h-24 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">아직 체크리스트가 없습니다</h3>
            <p className="text-gray-600">새로운 체크리스트를 만들어보세요!</p>
          </div>
        ) : (
          <div className="space-y-4">
            {checklists.map((checklist) => (
              <div
                key={checklist.id}
                className={`p-4 bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow cursor-pointer ${
                  isPastChecklist(checklist) ? 'opacity-60' : ''
                }`}
                onClick={() => checklist.isActive && handleChecklistClick(checklist)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    {/* 일정 제목 */}
                    <h3 className={`text-lg font-semibold mb-2 ${
                      isPastChecklist(checklist) ? 'text-gray-500' : 'text-gray-900'
                    }`}>
                      {checklist.schedule?.title || '제목 없음'}
                    </h3>
                    
                    {/* 날짜 및 위치 정보 */}
                    {checklist.schedule && (
                      <div className="flex items-center gap-4 text-sm text-gray-600">
                        <div className="flex items-center gap-1">
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3a1 1 0 012 0v4h4V3a1 1 0 012 0v4h.586l.707.707c.39.39.586.902.586 1.414v11.172a2 2 0 01-.586 1.414l-.707.707H7.414l-.707-.707A2 2 0 016 18.586V7.414c0-.512.196-1.024.586-1.414L7.293 5.293A1 1 0 018 5z" />
                          </svg>
                          <span>{formatDateRange(checklist.schedule.startDate || '', checklist.schedule.endDate || '')}</span>
                        </div>
                        
                        {checklist.schedule.spot && (
                          <div className="flex items-center gap-1">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                            </svg>
                            <span>{checklist.schedule.spot}</span>
                          </div>
                        )}
                      </div>
                    )}
                    
                    {/* 체크리스트 진행 상황 */}
                    {checklist.checkListItems && checklist.checkListItems.length > 0 && (
                      <div className="mt-3">
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 6h6m-6 4h6" />
                          </svg>
                          <span>
                            {checklist.checkListItems.filter(item => item.isChecked).length} / {checklist.checkListItems.length} 완료
                          </span>
                        </div>
                      </div>
                    )}
                    
                    {/* 상태 태그 */}
                    <div className="flex items-center gap-2 mt-3">
                      {isPastChecklist(checklist) && (
                        <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                          지난 일정
                        </span>
                      )}
                      {checklist.isActive ? (
                        <span className="px-2 py-1 bg-green-100 text-green-600 text-xs rounded-full">
                          활성
                        </span>
                      ) : (
                        <span className="px-2 py-1 bg-red-100 text-red-600 text-xs rounded-full">
                          비활성
                        </span>
                      )}
                    </div>
                  </div>
                  
                  {/* 액션 버튼 */}
                  {checklist.isActive && (
                    <div className="ml-4">
                      <button
                        className="px-3 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors text-sm font-medium"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleChecklistClick(checklist);
                        }}
                      >
                        상세보기
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}