'use client';

import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';

import { createSchedule, modifySchedule, getSchedule } from '@/api/schedule';
import type { ScheduleCreateReqBody, ScheduleUpdateReqBody } from '@/types/schedule';

// 동적 시간을 계산하는 헬퍼 함수
const getInitialTimes = () => {
  const now = new Date();
  const currentMinutes = now.getMinutes();
  let hour = now.getHours();

  // 현재 시간이 정시가 아니면 다음 정시로 올림
  if (currentMinutes > 0) {
    hour = (hour + 1) % 24;
  }

  const nextHour = String(hour).padStart(2, '0');
  const nextHourPlusOne = String((hour + 1) % 24).padStart(2, '0');

  return {
    startTime: `${nextHour}:00`,
    endTime: `${nextHourPlusOne}:00`,
  };
};

// props
interface ScheduleModalProps {
  showModal: boolean;
  clubId: number;
  startDate?: string;
  endDate?: string;
  selectedScheduleId?: number | null;
  onClose: (shouldRefresh: boolean) => void;
}

export default function ScheduleModal({
  showModal,
  clubId,
  startDate,
  endDate,
  selectedScheduleId,
  onClose,
}: ScheduleModalProps) {
  // 일정 아이디있으면 수정 모드
  const isEditing = !!selectedScheduleId;

  // 생성/수정 폼 데이터 상태 관리
  const [scheduleData, setScheduleData] = useState<ScheduleCreateReqBody | ScheduleUpdateReqBody>({
    clubId,
    title: '',
    content: '',
    startDate: startDate || '',
    endDate: endDate || '',
    spot: '',
  });

  // 시작/종료 시간 상태를 빈 상태로 초기화
  const [scheduleTimes, setScheduleTimes] = useState({
    startTime: '',
    endTime: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 종료일-종료시간이 시작일-시작시간보다 앞서는지 체크하는 함수
  const isEndBeforeStart = (
    startDate: string,
    startTime: string,
    endDate: string,
    endTime: string
  ) => {
    if (!startDate || !startTime || !endDate || !endTime) return false;
    const start = new Date(`${startDate}T${startTime}:00`);
    const end = new Date(`${endDate}T${endTime}:00`);
    return end < start;
  };

  // 수정 모드일 때 상세 정보 불러오기
  useEffect(() => {
    if (!showModal) {
      return;
    }

    if (isEditing) {
      setIsLoading(true);

      const fetchDetail = async () => {
        try {
          // 일정 상세 조회
          const data = await getSchedule(selectedScheduleId);
          const detail = data.data;
          
          if (!detail) {
            throw new Error(data.message || '일정 정보를 불러올 수 없습니다.');
          }
          
          // 상세 정보 세팅
          setScheduleData({
            clubId,
            title: detail.title || '',
            content: detail.content || '',
            startDate: detail.startDate ? detail.startDate.split('T')[0] : '',
            endDate: detail.endDate ? detail.endDate.split('T')[0] : '',
            spot: detail.spot || '',
          });
          setScheduleTimes({
            startTime: detail.startDate ? detail.startDate.split('T')[1].substring(0, 5) : '09:00',
            endTime: detail.endDate ? detail.endDate.split('T')[1].substring(0, 5) : '10:00',
          });
        } catch (e) {
          const msg = e instanceof Error ? e.message : '일정 불러오기 실패하였습니다.';
          setError(msg);
          toast.error(msg);
        } finally {
          setIsLoading(false);
        }
      };
      fetchDetail();
    } 
    // 생성 모드일 때 폼 상태 초기화
    else {
      // 날짜/시간 데이터 파싱 로직 추가
      let initialStartDate = startDate || '';
      let initialEndDate = endDate || '';

      const { startTime, endTime } = getInitialTimes();
      let initialStartTime = startTime;
      let initialEndTime = endTime;

      // 시간 정보가 포함된 경우 (timeGridWeek 뷰)
      if (initialStartDate.includes('T')) {
        initialStartTime = initialStartDate.split('T')[1].substring(0, 5);
        initialEndTime = initialEndDate?.split('T')[1].substring(0, 5) || endTime;
        initialStartDate = initialStartDate.split('T')[0];
        initialEndDate = initialEndDate?.split('T')[0] || '';
      } else {
        // 시간 정보가 없을 때 (dayGridMonth 뷰)
        // 캘린더 라이브러리는 종료일의 다음 날을 반환하므로, 하루를 빼야함
        if (initialEndDate && initialStartDate !== initialEndDate) {
            const end = new Date(initialEndDate);
            end.setDate(end.getDate() - 1);
            initialEndDate = end.toISOString().split('T')[0];
        } else {
            initialEndDate = initialStartDate;
        }
      }
      // 일정 세팅
      setScheduleTimes({ startTime: initialStartTime, endTime: initialEndTime });
      setScheduleData(prev => ({
        ...prev,
        startDate: initialStartDate,
        endDate: initialEndDate,
      }));
    }
  }, [selectedScheduleId, isEditing, showModal, clubId, startDate, endDate]);

  // 폼 입력값 변경 핸들러
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (type === 'time') {
      // 종료시간/시작시간 입력 시, 종료가 시작보다 앞서면 종료시간을 자동으로 시작시간과 같게 맞추거나 무시
      if (name === 'endTime') {
        // 미리보기로 입력값을 적용해보고, 만약 종료가 시작보다 앞서면 무시
        if (
          isEndBeforeStart(
            scheduleData.startDate,
            scheduleTimes.startTime,
            scheduleData.endDate,
            value
          )
        ) {
          // 종료시간이 시작시간보다 앞서면 종료시간을 시작시간과 같게 맞춤
          setScheduleTimes(prev => ({ ...prev, endTime: prev.startTime }));
          toast.error('종료시간은 시작시간보다 같거나 이후여야 합니다.');
          return;
        }
      }
      if (name === 'startTime') {
        // 시작시간을 바꿀 때, 종료시간이 더 앞서면 종료시간도 같이 맞춰줌
        if (
          isEndBeforeStart(
            scheduleData.startDate,
            value,
            scheduleData.endDate,
            scheduleTimes.endTime
          )
        ) {
          setScheduleTimes(prev => ({ ...prev, startTime: value, endTime: value }));
          return;
        }
      }
      setScheduleTimes(prev => ({ ...prev, [name]: value }));
    } else if (type === 'date') {
      // 종료일/시작일 입력 시, 종료가 시작보다 앞서면 종료일을 시작일과 같게 맞추거나 무시
      if (name === 'endDate') {
        if (
          isEndBeforeStart(
            scheduleData.startDate,
            scheduleTimes.startTime,
            value,
            scheduleTimes.endTime
          )
        ) {
          setScheduleData(prev => ({ ...prev, endDate: prev.startDate }));
          toast.error('종료일은 시작일보다 같거나 이후여야 합니다.');
          return;
        }
      }
      if (name === 'startDate') {
        if (
          isEndBeforeStart(
            value,
            scheduleTimes.startTime,
            scheduleData.endDate,
            scheduleTimes.endTime
          )
        ) {
          setScheduleData(prev => ({ ...prev, startDate: value, endDate: value }));
          return;
        }
      }
      setScheduleData(prev => ({ ...prev, [name]: value }));
    } else {
      setScheduleData(prev => ({ ...prev, [name]: value }));
    }
  };

  // 생성/수정 폼 제출
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    // 종료일-종료시간이 시작일-시작시간보다 앞서는지 체크
    if (
      isEndBeforeStart(
        scheduleData.startDate,
        scheduleTimes.startTime,
        scheduleData.endDate,
        scheduleTimes.endTime
      )
    ) {
      setIsLoading(false);
      setError('종료일과 종료시간은 시작일과 시작시간보다 같거나 이후여야 합니다.');
      toast.error('종료일과 종료시간은 시작일과 시작시간보다 같거나 이후여야 합니다.');
      return;
    }

    try {
      // 날짜와 시간을 결합하여 date-time 형식으로 변환
      const transformedData = {
        ...scheduleData,
        startDate: `${scheduleData.startDate}T${scheduleTimes.startTime}:00`,
        endDate: `${scheduleData.endDate}T${scheduleTimes.endTime}:00`,
      };
      if (isEditing) {
        // 수정 API 호출
        const data = await modifySchedule(selectedScheduleId, transformedData);
        toast.success(data.message || '일정이 수정되었습니다.');
      } else {
        // 생성 API 호출
        const data = await createSchedule(transformedData as ScheduleCreateReqBody);
        toast.success(data.message || '일정이 생성되었습니다.');
      }
      // 성공 시 폼 필드 초기화
      setScheduleData(prev => ({
        ...prev,
        title: '',
        content: '',
        spot: '',
      }));
      const { startTime, endTime } = getInitialTimes();
      setScheduleTimes({ startTime, endTime });

      // 캘린더로 이동
      onClose(true);
    } catch (e: any) {
      const msg = e instanceof Error ? e.message : '저장에 실패하였습니다.';
      setError(msg);
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };
  
  if (!showModal) return null;

  // 종료일-종료시간이 시작일-시작시간보다 앞서는지 체크해서 버튼 비활성화
  const isInvalidEnd = isEndBeforeStart(
    scheduleData.startDate,
    scheduleTimes.startTime,
    scheduleData.endDate,
    scheduleTimes.endTime
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-xl p-6" style={{ width: '66vw', height: 'auto', maxHeight: '90vh' }}>
        <form onSubmit={handleSubmit} className="flex flex-col space-y-4 h-full">
          <div className="mb-4 pb-2 border-b border-gray-300">
            <h3 className="text-2xl font-bold text-gray-900 text-center">
              {isEditing ? '일정 수정' : '새 일정 생성'}
            </h3>
          </div>
          <div className="flex-grow space-y-4 px-2 overflow-y-auto">
            <div>
              <label className="block text-gray-700 font-semibold mb-1">제목</label>
              <input
                type="text"
                name="title"
                value={scheduleData.title}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-black placeholder-gray-400"
                required
                placeholder="제목을 입력하세요"
              />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">일시</label>
              <div className="flex items-center space-x-2 w-full">
                <input
                  type="date"
                  name="startDate"
                  value={scheduleData.startDate}
                  onChange={handleChange}
                  className="flex-1 min-w-0 p-2 border border-blue-400 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-blue-900 bg-blue-50 placeholder-blue-300"
                  required
                />
                <input
                  type="time"
                  name="startTime"
                  value={scheduleTimes.startTime}
                  onChange={handleChange}
                  className="flex-1 min-w-0 p-2 border border-blue-400 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-blue-900 bg-blue-50 placeholder-blue-300"
                  required
                />
                <span className="mx-1 text-lg text-gray-500 font-bold">~</span>
                <input
                  type="date"
                  name="endDate"
                  value={scheduleData.endDate}
                  onChange={handleChange}
                  className="flex-1 min-w-0 p-2 border border-blue-400 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-blue-900 bg-blue-50 placeholder-blue-300"
                  required
                  min={scheduleData.startDate}
                />
                <input
                  type="time"
                  name="endTime"
                  value={scheduleTimes.endTime}
                  onChange={handleChange}
                  className="flex-1 min-w-0 p-2 border border-blue-400 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-blue-900 bg-blue-50 placeholder-blue-300"
                  required
                  min={scheduleData.startDate === scheduleData.endDate ? scheduleTimes.startTime : undefined}
                />
              </div>
              {isInvalidEnd && (
                <div className="text-red-500 text-sm mt-1">종료일과 종료시간은 시작일과 시작시간보다 같거나 이후여야 합니다.</div>
              )}
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">장소</label>
              <input
                type="text"
                name="spot"
                value={scheduleData.spot}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-black placeholder-gray-400"
                placeholder="장소를 입력하세요"
              />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">내용</label>
              <textarea
                name="content"
                value={scheduleData.content}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded-md h-24 focus:outline-none focus:ring-2 focus:ring-blue-500 text-black placeholder-gray-400"
                placeholder="내용을 입력하세요"
              ></textarea>
            </div>
          </div>
          <div className="flex justify-end space-x-2 mt-auto">
            <button
              type="submit"
              disabled={isLoading || isInvalidEnd}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors disabled:opacity-50"
            >
              {isEditing ? '수정' : '생성'}
            </button>
            <button
              type="button"
              onClick={() => onClose(false)}
              disabled={isLoading} 
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}