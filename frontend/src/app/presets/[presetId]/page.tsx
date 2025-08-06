'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { Preset, Category } from '@/types/preset';
import { fetchPresetDetail, updatePreset, deletePreset } from '@/api/presetApi';
import PresetItem from '@/components/domain/preset/PresetItem';
import EditablePresetItem from '@/components/domain/preset/EditablePresetItem';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import ErrorMessage from '@/components/global/ErrorMessage';
import { DndContext, closestCenter, DragEndEvent } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, arrayMove } from '@dnd-kit/sortable';

const CATEGORIES: Category[] = ['PREPARATION', 'RESERVATION', 'PRE_WORK', 'ETC'];

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES: Record<Category, string> = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
};

// 데모 데이터
const DEMO_PRESETS: Record<string, Preset> = {
  '1': {
    id: 1,
    name: '준비물 - 펜션ver',
    presetItems: [
      { id: 1, content: '삼겹살, 간식', category: 'PREPARATION', sequence: 1 },
      { id: 2, content: '세면 용품', category: 'PREPARATION', sequence: 2 },
      { id: 3, content: '잠옷', category: 'PREPARATION', sequence: 3 },
      { id: 4, content: '펜션 예약 확인', category: 'RESERVATION', sequence: 4 },
      { id: 5, content: '주차장 예약', category: 'RESERVATION', sequence: 5 },
    ],
  },
  '2': {
    id: 2,
    name: '준비물 - 등산ver',
    presetItems: [
      { id: 6, content: '등산스틱', category: 'PREPARATION', sequence: 1 },
      { id: 7, content: '체인', category: 'PREPARATION', sequence: 2 },
      { id: 8, content: '물', category: 'PREPARATION', sequence: 3 },
      { id: 9, content: '등산로 확인', category: 'PRE_WORK', sequence: 4 },
      { id: 10, content: '날씨 체크', category: 'PRE_WORK', sequence: 5 },
      { id: 11, content: '보험 가입', category: 'ETC', sequence: 6 },
    ],
  },
  '3': {
    id: 3,
    name: '예약 - 국내 ver',
    presetItems: [
      { id: 12, content: '펜션 예약하기', category: 'RESERVATION', sequence: 1 },
      { id: 13, content: '쏘카 빌리기', category: 'RESERVATION', sequence: 2 },
      { id: 14, content: '레스토랑 예약', category: 'RESERVATION', sequence: 3 },
      { id: 15, content: '여행 계획 세우기', category: 'PRE_WORK', sequence: 4 },
      { id: 16, content: '경로 확인', category: 'PRE_WORK', sequence: 5 },
    ],
  },
};

export default function PresetDetailPage() {
  const params = useParams();
  const router = useRouter();
  const presetId = params.presetId as string;
  
  const [preset, setPreset] = useState<Preset | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoginRequired, setIsLoginRequired] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newItemName, setNewItemName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<Category>('PREPARATION');
  const [isEditMode, setIsEditMode] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editPresetName, setEditPresetName] = useState('');

  const loadPresetDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      setIsLoginRequired(false);
      const data = await fetchPresetDetail(presetId);
      setPreset(data);
      setEditPresetName(data.name || '');
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      // 401 에러인 경우 (로그인 필요)
      if (errorMessage.startsWith('LOGIN_REQUIRED:')) {
        setIsLoginRequired(true);
        // 해당 ID의 데모 데이터 설정
        const demoPreset = DEMO_PRESETS[presetId];
        if (demoPreset) {
          setPreset(demoPreset);
          setEditPresetName(demoPreset.name || '');
        } else {
          setError('프리셋을 찾을 수 없습니다.');
        }
      } else {
        setError('프리셋을 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (presetId) {
      loadPresetDetail();
    }
  }, [presetId]);

  const handleToggleComplete = (itemId: number) => {
    if (!preset) return;
    
    setPreset({
      ...preset,
      presetItems: preset.presetItems?.map(item =>
        item.id === itemId ? { ...item, completed: !item.completed } : item
      ) || []
    });
  };

  const handleDeleteItem = (itemId: number) => {
    if (!preset) return;
    
    if (confirm('정말로 이 아이템을 삭제하시겠습니까?')) {
      setPreset({
        ...preset,
        presetItems: preset.presetItems?.filter(item => item.id !== itemId) || []
      });
    }
  };

  const handleUpdateItem = (itemId: number, content: string, category: Category) => {
    if (!preset) return;
    
    setPreset({
      ...preset,
      presetItems: preset.presetItems?.map(item =>
        item.id === itemId ? { ...item, content, category } : item
      ) || []
    });
  };

  const handleEditPreset = () => {
    setEditPresetName(preset?.name || '');
    setIsEditMode(true);
  };

  const handleSavePreset = async () => {
    if (!preset || !editPresetName.trim()) return;
    
    try {
      setSaving(true);
      const updatedPreset = {
        ...preset,
        name: editPresetName.trim()
      };
      await updatePreset(presetId, updatedPreset);
      setPreset(updatedPreset);
      setIsEditMode(false);
      alert('프리셋이 성공적으로 수정되었습니다.');
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      alert('프리셋 수정 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancelEdit = () => {
    setEditPresetName(preset?.name || '');
    setIsEditMode(false);
    setShowAddForm(false); // 아이템 추가 폼 닫기
    setNewItemName(''); // 입력값 초기화
    setSelectedCategory('PREPARATION'); // 카테고리 초기화
    // 원본 데이터로 복원
    loadPresetDetail();
  };

  const handleDeletePreset = async () => {
    if (!preset) return;

    if (confirm('정말로 이 프리셋을 삭제하시겠습니까?')) {
      try {
        await deletePreset(presetId);
        alert('프리셋이 성공적으로 삭제되었습니다.');
        router.push('/presets');
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
        
        alert('프리셋 삭제 중 오류가 발생했습니다.');
      }
    }
  };

  const handleAddItem = () => {
    setShowAddForm(true);
  };

  const handleSubmitNewItem = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newItemName.trim() || !preset) return;
    
    const newItem = {
      id: Date.now(),
      content: newItemName.trim(),
      category: selectedCategory,
      sequence: (preset.presetItems?.length || 0) + 1,
    };
    
    setPreset({
      ...preset,
      presetItems: [...(preset.presetItems || []), newItem]
    });
    
    setNewItemName('');
    setSelectedCategory('PREPARATION');
    setShowAddForm(false);
  };

  const handleCancelAdd = () => {
    setNewItemName('');
    setSelectedCategory('PREPARATION');
    setShowAddForm(false);
  };

  // 드래그 앤 드롭 핸들러
  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!preset || !over || active.id === over.id) {
      return;
    }

    const oldIndex = preset.presetItems?.findIndex(item => item.id === active.id) ?? -1;
    const newIndex = preset.presetItems?.findIndex(item => item.id === over.id) ?? -1;

    if (oldIndex !== -1 && newIndex !== -1) {
      const reorderedItems = arrayMove(preset.presetItems || [], oldIndex, newIndex);
      
      // sequence 번호 재설정
      const updatedItems = reorderedItems.map((item, index) => ({
        ...item,
        sequence: index + 1
      }));

      setPreset({
        ...preset,
        presetItems: updatedItems
      });
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

  if (error && !isLoginRequired) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <ErrorMessage message={error} onRetry={loadPresetDetail} />
        </div>
      </div>
    );
  }

  if (!preset) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="text-center">
            <h2 className="text-xl font-semibold text-gray-800 mb-2">프리셋을 찾을 수 없습니다</h2>
            <Link
              href="/presets"
              className="text-blue-500 hover:text-blue-600 underline"
            >
              프리셋 목록으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <Link
                href="/presets"
                className="text-gray-600 hover:text-gray-800 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </Link>
              {isEditMode ? (
                <input
                  type="text"
                  value={editPresetName}
                  onChange={(e) => setEditPresetName(e.target.value)}
                  placeholder="프리셋 이름을 입력하세요"
                  className="text-3xl font-bold text-gray-900 bg-transparent border-b-2 border-blue-500 focus:outline-none focus:border-blue-600 px-2 py-1"
                />
              ) : (
                <h1 className="text-3xl font-bold text-gray-900">{preset.name}</h1>
              )}
            </div>
            
            {/* 액션 버튼들 */}
            <div className="flex gap-2">
              {isEditMode ? (
                <>
                  <button
                    onClick={handleSavePreset}
                    disabled={saving || !editPresetName.trim()}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                  >
                    {saving ? '저장 중...' : '수정하기'}
                  </button>
                  <button
                    onClick={handleCancelEdit}
                    disabled={saving}
                    className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                  >
                    취소
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={handleEditPreset}
                    disabled={isLoginRequired}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                    title={isLoginRequired ? "로그인이 필요합니다" : "수정"}
                  >
                    수정
                  </button>
                  <button
                    onClick={handleDeletePreset}
                    disabled={isLoginRequired}
                    className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                    title={isLoginRequired ? "로그인이 필요합니다" : "삭제"}
                  >
                    삭제
                  </button>
                </>
              )}
            </div>
          </div>
          {isEditMode && (
            <p className="text-gray-600 text-sm mt-2">프리셋 이름과 아이템들을 수정할 수 있습니다</p>
          )}
        </div>


        {/* 프리셋 아이템 목록 */}
        <div className="mb-6">
          {preset && preset.presetItems && preset.presetItems.length > 0 ? (
            isEditMode ? (
              <DndContext
                collisionDetection={closestCenter}
                onDragEnd={handleDragEnd}
              >
                <SortableContext
                  items={preset.presetItems.map(item => item.id)}
                  strategy={verticalListSortingStrategy}
                >
                  <div className="space-y-3">
                    {preset.presetItems.map((item) => (
                      <EditablePresetItem
                        key={item.id}
                        item={item}
                        onUpdate={handleUpdateItem}
                        onDelete={handleDeleteItem}
                      />
                    ))}
                  </div>
                </SortableContext>
              </DndContext>
            ) : (
              <div className="space-y-3">
                {preset.presetItems.map((item) => (
                  <div key={item.id} className="flex items-center gap-3 p-3 bg-white rounded-lg border border-gray-100 hover:shadow-sm transition-shadow">
                    {/* 카테고리 태그 */}
                    <div className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                      {item.category ? CATEGORY_DISPLAY_NAMES[item.category as Category] : '준비물'}
                    </div>
                    
                    {/* 아이템 이름 */}
                    <span className="flex-1 text-gray-800">
                      {item.content || '내용 없음'}
                    </span>
                  </div>
                ))}
              </div>
            )
          ) : (
            <div className="text-center py-8 text-gray-500">
              아직 아이템이 없습니다.
            </div>
          )}
        </div>

        {/* 아이템 추가 폼 */}
        {showAddForm && (
          <div className="mb-6 p-4 bg-white rounded-lg border border-gray-200 shadow-sm">
            <form onSubmit={handleSubmitNewItem} className="space-y-4">
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
                <input
                  type="text"
                  value={newItemName}
                  onChange={(e) => setNewItemName(e.target.value)}
                  placeholder="새 아이템 이름을 입력하세요"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                  autoFocus
                />
              </div>
              
              {/* 버튼들 */}
              <div className="flex gap-3">
                <button
                  type="submit"
                  disabled={!newItemName.trim()}
                  className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                >
                  추가
                </button>
                <button
                  type="button"
                  onClick={handleCancelAdd}
                  className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        )}

        {/* 아이템 추가 버튼 */}
        {!showAddForm && isEditMode && (
          <button
            onClick={handleAddItem}
            className="w-12 h-12 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors flex items-center justify-center shadow-lg"
            title="아이템 추가"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </button>
        )}
      </div>
    </div>
  );
} 