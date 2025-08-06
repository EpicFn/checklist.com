'use client';

import { useSearchParams, useRouter } from 'next/navigation';
import { useState, useCallback, useRef, useEffect } from 'react';
import toast from 'react-hot-toast';

import { EventClickArg, DateSelectArg, DatesSetArg } from '@fullcalendar/core';
import FullCalendar from '@fullcalendar/react';

import { getClubSchedules, getMyClubInfoForSchedule } from "@/api/schedule";
import { useSchedules } from '@/hooks/useSchedules';
import { extractDateFromISO } from '@/lib/formatDate';
import Calendar from '@/components/domain/schedule/Calender';
import ScheduleModal from '@/app/schedule/modals/ScheduleModal';
import ScheduleEditModal from '@/app/schedule/modals/ScheduleEditModal';

import '@/lib/fullcalendar.css';

export default function ScheduleListPage() {
  // 서치 파라미터 처리 - 모임 아이디
  const searchParams = useSearchParams();
  const clubId = Number(searchParams.get('clubId'));

  if (isNaN(clubId)) {
    throw new Error('유효하지 않은 모임입니다.');
  }

  const router = useRouter();

  // 캘린더 처리
  const calendarRef = useRef<FullCalendar>(null);
  const [selectedDateInfo, setSelectedDateInfo] = useState<DateSelectArg | null>(null);
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(null);

  // 모달 상태 관리
  const [showModal, setShowModal] = useState<boolean>(false);
  const [modalType, setModalType] = useState<'edit' | 'detail' | null>(null);

  // 리드온리 여부
  const [isReadOnly, setIsReadOnly] = useState<boolean>(true);

  // 커스텀 훅 - 일정 dto, 모임의 일정 목록 조회 fetch 넘김
  const { events, fetchSchedules } = useSchedules((params, signal) => 
    getClubSchedules(clubId, params, signal)
  );

  // 최초 조회
  useEffect(() => {
    // 캘린더 API를 통해 현재 뷰의 날짜 범위를 가져와서 데이터 조회
    if (calendarRef.current) {
      const calendarApi = calendarRef.current.getApi();
      const view = calendarApi.view;
      const startDate = extractDateFromISO(view.activeStart.toISOString());
      const endDate = extractDateFromISO(view.activeEnd.toISOString());
      fetchSchedules({ startDate, endDate });
    }

    // 유저의 모임 정보 조회
    getMyClubInfoForSchedule(clubId)
      .then(res => {
        if (res.data?.role) {
          // 모임 일반 참여자는 조회만 가능
          setIsReadOnly(res.data.role === 'PARTICIPANT');
        }
      })
      .catch(e => {
        toast.error("모임 정보를 불러오지 못했습니다.");
      });
  }, [clubId]);

  // 캘린더 날짜가 변경될 때마다(이전, 다음 버튼 등) 일정 목록 API 재호출
  const handleDatesSet = useCallback((arg: DatesSetArg) => {
    const startDate = extractDateFromISO(arg.startStr);
    const endDate = extractDateFromISO(arg.endStr);
    fetchSchedules({ startDate, endDate });
  }, [fetchSchedules]);

  // Date UI(일) 클릭 시 일정 생성/수정정 모달
  const handleDateSelect = (selectInfo: DateSelectArg) => {
    if (isReadOnly) return;

    // 모임장, 매니저만 가능
    setModalType('edit');
    setSelectedDateInfo(selectInfo);
    setSelectedScheduleId(null);
    setShowModal(true);
  };

  // 일정 클릭 시 일정 상세 모달
  const handleEventClick = (clickInfo: EventClickArg) => {
    setModalType('detail');
    setSelectedScheduleId(Number(clickInfo.event.id)); // ID만 저장
    setSelectedDateInfo(null);
    setShowModal(true);
  };

  // 일정 생성/삭제 후 캘린더를 최신화
  const refreshCalendar = async () => {
    if (calendarRef.current) {
      const calendarApi = calendarRef.current.getApi();
      const view = calendarApi.view;
      const startDate = extractDateFromISO(view.activeStart.toISOString());
      const endDate = extractDateFromISO(view.activeEnd.toISOString());
      // 일정 목록 재조회
      await fetchSchedules({ startDate, endDate });
    }
  };

  // 모달 닫기
  const handleCloseModal = useCallback((
    shouldRefresh: boolean, 
    action?: 'modify' | 'goToCheckList' | 'createCheckList', 
    targetId?: number
  ) => {
    setShowModal(false);
    setSelectedDateInfo(null);
    setModalType(null);
    setSelectedScheduleId(null);
    
    if (action === 'modify') {
      // 상세 -> 수정버튼 클릭 -> 수정 모드
      setSelectedScheduleId(targetId || null);
      setModalType('edit');
      setShowModal(true);
    } else if (action === 'goToCheckList') {
      toast.success('체크리스트로 이동합니다.'); 
      router.push(`checklists/${targetId}?clubId=${clubId}`)
    } else if (action === 'createCheckList') {
      toast.success('체크리스트 생성 페이지로 이동합니다.'); 
      router.push(`/checklists/create?clubId=${clubId}&scheduleId=${targetId}`)
    }

    // 캘린더 새로 고침
    if (shouldRefresh) {
      refreshCalendar();
    }
  }, [refreshCalendar]);

  return (
    <div className='flex flex-col lg:flex-row min-h-screen bg-gray-100 font-sans'>
      {/* Main Calendar */}
      <div className='flex-1 p-4 lg:p-6'>
        <Calendar 
          ref={calendarRef}
          events={events} 
          handleDatesSet={handleDatesSet} 
          handleEventClick={handleEventClick}
          handleDateSelect={handleDateSelect}
          />
        {showModal && modalType === 'detail' && (
          <ScheduleModal
            showModal={showModal}
            selectedScheduleId={selectedScheduleId}
            onClose={handleCloseModal}
            isReadOnly={isReadOnly}
          />
        )}
        {showModal && modalType === 'edit' && (
          <ScheduleEditModal
            showModal={showModal}
            clubId={clubId}
            startDate={selectedDateInfo?.startStr}
            endDate={selectedDateInfo?.endStr}
            selectedScheduleId={selectedScheduleId}
            onClose={handleCloseModal}
          />
        )}
      </div>
    </div>
  );
}
