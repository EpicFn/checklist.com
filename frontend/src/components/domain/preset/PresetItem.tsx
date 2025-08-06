'use client';

import type { PresetItem as PresetItemType, Category } from '@/types/preset';

interface PresetItemProps {
  item: PresetItemType;
  onToggleComplete: (itemId: string) => void;
  onDelete: (itemId: string) => void;
}

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES: Record<Category, string> = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
};

export default function PresetItem({ item, onToggleComplete, onDelete }: PresetItemProps) {
  return (
    <div className="flex items-center gap-3 p-3 bg-white rounded-lg border border-gray-100 hover:shadow-sm transition-shadow">
      {/* 카테고리 태그 */}
      <div className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
        {item.category ? CATEGORY_DISPLAY_NAMES[item.category as Category] : '준비물'}
      </div>
      
      {/* 체크박스 */}
      <button
        onClick={() => onToggleComplete(item.id)}
        className="flex-shrink-0"
      >
        <div className={`w-5 h-5 border-2 rounded flex items-center justify-center transition-colors ${
          item.completed 
            ? 'bg-green-500 border-green-500' 
            : 'border-gray-300 hover:border-green-400'
        }`}>
          {item.completed && (
            <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
          )}
        </div>
      </button>
      
      {/* 아이템 이름 */}
      <span className={`flex-1 text-gray-800 ${
        item.completed ? 'line-through text-gray-500' : ''
      }`}>
        {item.name}
      </span>
      
      {/* 삭제 버튼 */}
      <button
        onClick={() => onDelete(item.id)}
        className="flex-shrink-0 text-red-400 hover:text-red-600 transition-colors p-1"
        title="삭제"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  );
} 