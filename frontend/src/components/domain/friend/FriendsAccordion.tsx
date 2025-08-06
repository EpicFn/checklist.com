'use client';

import React from 'react';

interface AccordionPanelProps {
  title: React.ReactNode;
  count: number;
  isOpen: boolean;
  onToggle: () => void;
  children: React.ReactNode;
}

// 친구 목록 아코디언
const AccordionPanel = ({ title, count, isOpen, onToggle, children }: AccordionPanelProps) => {
  return (
    <div className="mb-4 border border-gray-200 rounded-xl overflow-hidden bg-white shadow-sm w-full max-w-[540px] transition-shadow">
      <div
        onClick={onToggle}
        className={`flex justify-between items-center px-4 py-2.5 cursor-pointer font-bold text-gray-700 text-[1.05rem] select-none transition-colors min-h-[36px] h-9 ${
          isOpen ? 'bg-gray-50 border-b border-gray-200' : 'bg-white'
        }`}
      >
        <span>
          {title}{' '}
          <span className="text-gray-400 font-semibold text-[14px]">({count})</span>
        </span>
        <span
          className={`text-[1.15em] font-bold ml-2 transition-colors ${
            isOpen ? 'text-blue-500' : 'text-gray-300'
          }`}
        >
          {isOpen ? '▲' : '▼'}
        </span>
      </div>
      {isOpen && <div className="px-4 pt-3 pb-2 bg-white">{children}</div>}
    </div>
  );
};

export default AccordionPanel;