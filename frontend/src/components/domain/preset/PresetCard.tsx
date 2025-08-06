'use client';

import { Preset } from '@/types/preset';
import { useState } from 'react';

interface PresetCardProps {
  preset: Preset;
  onClick: (preset: Preset) => void;
}

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES: Record<string, string> = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
};

export default function PresetCard({ preset, onClick }: PresetCardProps) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div
      className="bg-white rounded-lg shadow-md p-6 cursor-pointer transition-all duration-200 hover:shadow-lg hover:scale-105 border border-gray-100"
      onClick={() => onClick(preset)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between items-start mb-4">
        <h3 className="text-lg font-semibold text-gray-800 truncate">
          {preset.name}
        </h3>
        <div className="flex space-x-2">
          <button className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
            </svg>
          </button>
        </div>
      </div>
      
      <div className="space-y-2">
        {preset.presetItems.slice(0, 3).map((item) => (
          <div key={item.id} className="flex items-center space-x-2">
            <div className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
              {item.category ? CATEGORY_DISPLAY_NAMES[item.category] : '준비물'}
            </div>
            <span className="text-sm text-gray-700">
              {item.content}
            </span>
          </div>
        ))}
        {preset.presetItems.length > 3 && (
          <div className="text-xs text-gray-500 pt-1">
            +{preset.presetItems.length - 3}개 더 보기
          </div>
        )}
      </div>
      
      <div className="mt-4 pt-3 border-t border-gray-100">
        <div className="flex justify-between items-center text-xs text-gray-500">
          <span>총 {preset.presetItems.length}개 항목</span>
        </div>
      </div>
    </div>
  );
} 