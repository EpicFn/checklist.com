'use client';

import React, { forwardRef } from 'react';

import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { EventContentArg, EventClickArg , DateSelectArg } from '@fullcalendar/core';

interface CalendarProps {
  events: any[];
  handleDatesSet: (arg: any) => void;
  handleEventClick: (clickInfo: EventClickArg) => void; 
  handleDateSelect?: (selectInfo: DateSelectArg) => void;
}

const Calendar = forwardRef<FullCalendar, CalendarProps>(({
  events, 
  handleDatesSet,
  handleEventClick,
  handleDateSelect
}, ref) => {

  // 이벤트 내용 렌더링 함수
  const renderEventContent = (eventInfo: EventContentArg) => (
    <>
      <b>{eventInfo.timeText}</b>
      <i>{eventInfo.event.title}</i>
    </>
  );

  return (
    <FullCalendar
      ref={ref}
      plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
      headerToolbar={{
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek'
      }}
      initialView='dayGridMonth'
      editable={true}
      selectable={true}
      selectMirror={true}
      dayMaxEvents={true}
      datesSet={handleDatesSet}
      eventContent={renderEventContent}
      locale='ko'
      height='auto'
      events={events}
      eventClick={handleEventClick}
      select={handleDateSelect}
    />
  );
});

export default Calendar;