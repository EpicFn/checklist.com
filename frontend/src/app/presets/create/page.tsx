'use client';

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Preset, Category, PresetItem, PresetWriteReqDto, PresetItemWriteReqDto } from '@/types/preset';
import { createPreset } from '@/api/presetApi';
import LoadingSpinner from '@/components/global/LoadingSpinner';

const CATEGORIES: Category[] = ['PREPARATION', 'RESERVATION', 'PRE_WORK', 'ETC'];

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES: Record<Category, string> = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
};

export default function CreatePresetPage() {
  const router = useRouter();
  
  const [presetName, setPresetName] = useState('');
  const [items, setItems] = useState<Array<{id: number, content: string, category: Category}>>([]);
  const [editingItemId, setEditingItemId] = useState<number | null>(null);
  const [editingContent, setEditingContent] = useState('');
  const [editingCategory, setEditingCategory] = useState<Category>('PREPARATION');
  const [isAddingItem, setIsAddingItem] = useState(false);
  const [newItemName, setNewItemName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<Category>('PREPARATION');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAddItem = useCallback(() => {
    const trimmedName = newItemName.trim();
    if (!trimmedName || isAddingItem) return;
    
    setIsAddingItem(true);
    
    const newItem = {
      id: Date.now() + Math.random(),
      content: trimmedName,
      category: selectedCategory,
    };
    
    setItems(prevItems => [...prevItems, newItem]);
    setTimeout(() => {
      setNewItemName('');
      setSelectedCategory('PREPARATION');
      setIsAddingItem(false);
    }, 0);
  }, [newItemName, selectedCategory, isAddingItem]);

  const handleEditItem = (itemId: number) => {
    const item = items.find(item => item.id === itemId);
    if (item) {
      setEditingItemId(itemId);
      setEditingContent(item.content);
      setEditingCategory(item.category);
    }
  };

  const handleSaveEdit = () => {
    if (!editingContent.trim()) return;
    
    setItems(items.map(item => 
      item.id === editingItemId 
        ? { ...item, content: editingContent.trim(), category: editingCategory }
        : item
    ));
    
    setEditingItemId(null);
    setEditingContent('');
    setEditingCategory('PREPARATION');
  };

  const handleCancelEdit = () => {
    setEditingItemId(null);
    setEditingContent('');
    setEditingCategory('PREPARATION');
  };

  const handleRemoveItem = (itemId: number) => {
    setItems(items.filter(item => item.id !== itemId));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!presetName.trim()) {
      setError('프리셋 이름을 입력해주세요.');
      return;
    }
    
    if (items.length === 0) {
      setError('최소 하나의 아이템을 추가해주세요.');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      
      // 자바 DTO 구조에 맞게 변환
      const presetWriteReqDto: PresetWriteReqDto = {
        name: presetName.trim(),
        presetItems: items.map((item, index) => ({
          content: item.content,
          category: item.category as Category,
          sequence: index + 1,
        })),
      };
      
      const createdPreset = await createPreset(presetWriteReqDto);
      alert('프리셋이 성공적으로 생성되었습니다.');
      router.push(`/presets/${createdPreset.id}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      setError('프리셋 생성 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (presetName.trim() || items.length > 0) {
      if (confirm('작성 중인 내용이 있습니다. 정말로 나가시겠습니까?')) {
        router.push('/presets');
      }
    } else {
      router.push('/presets');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-4">
            <Link
              href="/presets"
              className="text-gray-600 hover:text-gray-800 transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">새 프리셋 만들기</h1>
          </div>
          <p className="text-gray-600">새로운 프리셋을 만들어보세요</p>
        </div>

        {/* 에러 메시지 */}
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <div className="flex items-center gap-2 text-red-800">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-medium">오류</span>
            </div>
            <p className="text-red-700 text-sm mt-1">{error}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* 프리셋 이름 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              프리셋 이름 *
            </label>
            <input
              type="text"
              value={presetName}
              onChange={(e) => setPresetName(e.target.value)}
              placeholder="예: 준비물 - 여행ver"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
              required
            />
          </div>

          {/* 아이템 추가 섹션 */}
          <div>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">아이템 추가</h3>
            
            {/* 아이템 추가 폼 */}
            <div className="p-4 bg-white rounded-lg border border-gray-200 shadow-sm mb-4">
              <div className="space-y-4">
                {/* 카테고리 선택 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    카테고리
                  </label>
                  <div className="flex gap-2">
                    {CATEGORIES.map((category) => (
                      <button
                        key={category}
                        type="button"
                        onClick={() => setSelectedCategory(category)}
                        className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                          selectedCategory === category
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
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newItemName}
                      onChange={(e) => setNewItemName(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          e.stopPropagation();
                          if (newItemName.trim()) {
                            handleAddItem();
                          }
                        }
                      }}
                      placeholder="새 아이템 이름을 입력하세요"
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                    />
                    <button
                      type="button"
                      onClick={handleAddItem}
                      disabled={!newItemName.trim()}
                      className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                    >
                      추가
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* 아이템 목록 */}
            {items.length > 0 ? (
              <div className="space-y-2">
                {items.map((item) => (
                  <div key={item.id} className="p-3 bg-white rounded-lg border border-gray-100">
                    {editingItemId === item.id ? (
                      /* 편집 모드 */
                      <div className="space-y-3">
                        {/* 카테고리 선택 */}
                        <div className="flex gap-2">
                          {CATEGORIES.map((category) => (
                            <button
                              key={category}
                              type="button"
                              onClick={() => setEditingCategory(category)}
                              className={`px-2 py-1 rounded-lg text-xs font-medium transition-colors ${
                                editingCategory === category
                                  ? 'bg-blue-500 text-white'
                                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                              }`}
                            >
                              {CATEGORY_DISPLAY_NAMES[category]}
                            </button>
                          ))}
                        </div>
                        
                        {/* 내용 입력 */}
                        <div className="flex gap-2">
                          <input
                            type="text"
                            value={editingContent}
                            onChange={(e) => setEditingContent(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') {
                                e.preventDefault();
                                e.stopPropagation();
                                if (editingContent.trim()) {
                                  handleSaveEdit();
                                }
                              } else if (e.key === 'Escape') {
                                e.preventDefault();
                                e.stopPropagation();
                                handleCancelEdit();
                              }
                            }}
                            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                            autoFocus
                          />
                          <button
                            type="button"
                            onClick={handleSaveEdit}
                            disabled={!editingContent.trim()}
                            className="px-3 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                            title="저장"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                          </button>
                          <button
                            type="button"
                            onClick={handleCancelEdit}
                            className="px-3 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
                            title="취소"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    ) : (
                      /* 표시 모드 */
                      <div className="flex items-center gap-3">
                        <div className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                          {CATEGORY_DISPLAY_NAMES[item.category as Category]}
                        </div>
                        <span className="flex-1 text-gray-800">{item.content}</span>
                        <button
                          type="button"
                          onClick={() => handleEditItem(item.id)}
                          className="text-blue-400 hover:text-blue-600 transition-colors p-1"
                          title="수정"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          onClick={() => handleRemoveItem(item.id)}
                          className="text-red-400 hover:text-red-600 transition-colors p-1"
                          title="삭제"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                <svg className="w-12 h-12 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                </svg>
                <p>아이템을 추가해주세요</p>
              </div>
            )}
          </div>

          {/* 버튼들 */}
          <div className="flex gap-3 pt-6 border-t border-gray-200">
            <button
              type="submit"
              disabled={!presetName.trim() || items.length === 0}
              className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-medium"
            >
              프리셋 생성
            </button>
            <button
              type="button"
              onClick={handleCancel}
              className="px-6 py-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors font-medium"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}