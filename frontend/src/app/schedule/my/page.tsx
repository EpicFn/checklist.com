'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

import FullCalendar from '@fullcalendar/react';
import { EventClickArg, DatesSetArg } from '@fullcalendar/core';

import { getMySchedules } from "@/api/schedule";
import { useSchedules } from '@/hooks/useSchedules';
import { extractDateFromISO } from '@/lib/formatDate';
import Calendar from '@/components/domain/schedule/Calender';
import ScheduleModal from '@/app/schedule/modals/ScheduleModal';

import '@/lib/fullcalendar.css';

export default function MyScheduleListPage() {
  const calendarRef = useRef<FullCalendar>(null);
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(null);
  const [showModal, setShowModal] = useState<boolean>(false);

  // 커스텀 훅 - 일정 dto, 나의 일정 목록 조회 fetch 넘김
  const { events, fetchSchedules } = useSchedules(getMySchedules);

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
  }, []);


  // 캘린더 날짜가 변경될 때마다(이전, 다음 버튼 등) 일정 목록 API 재호출
  const handleDatesSet = useCallback((arg: DatesSetArg) => {
    const startDate = extractDateFromISO(arg.startStr);
    const endDate = extractDateFromISO(arg.endStr);
    fetchSchedules({ startDate, endDate });
  }, [fetchSchedules]);

  // 일정 클릭 시 상세 모달만 열기
  const handleEventClick = (clickInfo: EventClickArg) => {
    setSelectedScheduleId(Number(clickInfo.event.id));
    setShowModal(true);
  };
  
  // 모달 닫기
  const handleCloseModal = useCallback(() => {
    setShowModal(false);
    setSelectedScheduleId(null);
  }, []);

  return (
    <div className='flex flex-col lg:flex-row min-h-screen bg-gray-100 font-sans'>
      <div className='flex-1 p-4 lg:p-6'>
        <Calendar 
          ref={calendarRef}
          events={events} 
          handleDatesSet={handleDatesSet} 
          handleEventClick={handleEventClick}
        />
        {showModal && (
          <ScheduleModal
            showModal={showModal}
            selectedScheduleId={selectedScheduleId}
            onClose={handleCloseModal}
            isReadOnly={true}
          />
        )}
      </div>
    </div>
  );
}