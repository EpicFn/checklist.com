// hooks/useSchedules.ts

import { useState, useCallback, useRef, useEffect } from 'react';
import toast from 'react-hot-toast';

import type { components } from "@/types/backend/apiV1/schema";
import { EventInput } from '@fullcalendar/core';

type ScheduleDto = components["schemas"]["ScheduleDto"];
type ScheduleWithClubDto = components["schemas"]["ScheduleWithClubDto"];
type UnionScheduleDto = ScheduleDto & Partial<ScheduleWithClubDto>;
import { SCHEDULE_COLORS, COLOR_INDIGO } from '@/constants/colors';

// fetch params
type FetchParams = {
  startDate?: string;
  endDate?: string;
  clubId?: number;
};

// 일정 목록 공통 훅
export const useSchedules = <T extends UnionScheduleDto>(
  fetchApi: (params?: FetchParams, signal?: AbortSignal) => Promise<{ data?: T[] | null | undefined }>
) => {
  const [events, setEvents] = useState<EventInput[]>([]);
  const [error, setError] = useState<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // 모임마다 다른 색상의 bar
  const generateColor = (id: number) => {
    return SCHEDULE_COLORS[id % SCHEDULE_COLORS.length];
  };

  // ScheduleDto/ScheduleWithClubDto를 FullCalendar Event 객체로 변환
  const convertSchedulesToEvents = (schedules: T[]) => {
    return schedules.map(schedule => {
      // 나의 일정 목록은 클럽명 포함
      const hasClubInfo = (schedule as ScheduleWithClubDto).clubName !== undefined;
      const titlePrefix = hasClubInfo && (schedule as ScheduleWithClubDto).clubName ? `[${(schedule as ScheduleWithClubDto).clubName}] ` : '';

      const color = hasClubInfo && (schedule as ScheduleWithClubDto).clubId 
        ? generateColor((schedule as ScheduleWithClubDto).clubId!) 
        : COLOR_INDIGO;
      
      // 일정 정보 세팅
      return {
        id: schedule.id !== undefined ? String(schedule.id) : undefined,
        title: titlePrefix + (schedule.title || '제목 없음'),
        start: schedule.startDate || new Date().toISOString(),
        end: schedule.endDate,
        allDay: (schedule.startDate?.length || 0) <= 10 && (schedule.endDate?.length || 0) <= 10,
        color,
        display: 'block'
      };
    });
  };

  // 일정 목록 조회
  const fetchSchedules = useCallback(async (params: FetchParams) => {
    // 이전 요청 있으면 취소
    abortControllerRef.current?.abort();
    const controller = new AbortController();
    abortControllerRef.current = controller;

    setError(null);

    try {
      // 모임의 일정 목록은 모임 아이디 전달 받음(필수)
      if ('clubId' in params && !params.clubId) {
        setError('유효하지 않은 모임입니다.');
        return;
      }
      // 일정 목록 조회
      const data = await fetchApi(params, controller.signal);

      // 일정 목록 세팅
      const events = convertSchedulesToEvents(data.data ?? []);
      // 캘린더 이벤트 저장
      setEvents(events);
    } catch (e) {
      // 요청 취소는 무시
      if (e instanceof Error && e.name === 'AbortError') return;
      // 에러 처리
      const msg = e instanceof Error ? e.message : '일정 불러오기 실패';
      setError(msg);
      toast.error(msg);
    }
  }, [fetchApi]);

  // 컴포넌트 사라질 때 요청 취소
  useEffect(() => {
    return () => {
      abortControllerRef.current?.abort();
    };
  }, []);

  return { events, error, fetchSchedules };
};