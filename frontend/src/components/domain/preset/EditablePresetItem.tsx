'use client';

import type { PresetItem as PresetItemType, Category } from '@/types/preset';
import { useState, useEffect } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

interface EditablePresetItemProps {
  item: PresetItemType;
  onUpdate: (itemId: number, content: string, category: Category) => void;
  onDelete: (itemId: number) => void;
}

const CATEGORIES: Category[] = ['PREPARATION', 'RESERVATION', 'PRE_WORK', 'ETC'];

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES: Record<Category, string> = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
};

export default function EditablePresetItem({ item, onUpdate, onDelete }: EditablePresetItemProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState(item.content || '');
  const [editCategory, setEditCategory] = useState<Category>(item.category as Category || 'PREPARATION');

  // 드래그 앤 드롭 설정
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  // item이 변경될 때마다 로컬 상태 업데이트
  useEffect(() => {
    setEditName(item.content || '');
    setEditCategory(item.category as Category || 'PREPARATION');
  }, [item]);

  // 실시간으로 변경사항 반영
  const handleContentChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newContent = e.target.value;
    setEditName(newContent);
    // 즉시 업데이트
    onUpdate(item.id, newContent, editCategory);
  };

  // 카테고리 변경 시 즉시 반영
  const handleCategoryChange = (category: Category) => {
    setEditCategory(category);
    onUpdate(item.id, editName || item.content || '', category);
  };

  // 편집 모드 종료 (변경사항은 이미 반영됨)
  const handleFinishEditing = () => {
    setIsEditing(false);
  };

  // 키보드 이벤트 처리
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === 'Escape') {
      // Enter나 Esc 키를 누르면 편집 완료
      handleFinishEditing();
    }
  };

  if (isEditing) {
    return (
      <div className="p-4 bg-white rounded-lg border-2 border-blue-200 shadow-sm">
        <div className="space-y-4">
          {/* 카테고리 선택 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              카테고리
            </label>
            <div className="flex gap-2 flex-wrap">
              {CATEGORIES.map((category) => (
                <button
                  key={category}
                  type="button"
                  onClick={() => handleCategoryChange(category)}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    editCategory === category
                      ? 'bg-blue-500 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {CATEGORY_DISPLAY_NAMES[category]}
                </button>
              ))}
            </div>
          </div>
          
          {/* 아이템 이름 입력 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              아이템 이름
            </label>
            <input
              type="text"
              value={editName}
              onChange={handleContentChange}
              onKeyDown={handleKeyPress}
              onBlur={handleFinishEditing}
              placeholder="아이템 내용을 입력하세요"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 text-base"
              autoFocus
            />
            <p className="text-xs text-gray-500 mt-1">
              변경사항이 즉시 반영됩니다 (Enter 또는 Esc로 완료)
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`flex items-center gap-3 p-3 bg-white rounded-lg border border-gray-100 hover:shadow-sm transition-shadow ${
        isDragging ? 'shadow-lg' : ''
      }`}
    >
      {/* 드래그 핸들 */}
      <div
        {...attributes}
        {...listeners}
        className="flex-shrink-0 cursor-grab active:cursor-grabbing text-gray-400 hover:text-gray-600 p-1"
        title="드래그하여 순서 변경"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8h16M4 16h16" />
        </svg>
      </div>
      
      {/* 카테고리 태그 */}
      <div className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
        {item.category ? CATEGORY_DISPLAY_NAMES[item.category as Category] : '준비물'}
      </div>
      
      {/* 아이템 이름 */}
      <span className="flex-1 text-gray-800">
        {item.content || '내용 없음'}
      </span>
      
      {/* 수정 버튼 */}
      <button
        onClick={() => setIsEditing(true)}
        className="flex-shrink-0 text-blue-400 hover:text-blue-600 transition-colors p-1"
        title="수정"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
      </button>
      
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