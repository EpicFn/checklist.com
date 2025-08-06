'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { CheckListItem, CheckListWriteReqDto } from '@/types/checklist';
import { createChecklist, fetchGroupMembers, ClubMember, fetchGroupUserInfo, GroupUserInfo, fetchScheduleDetail, ScheduleInfo } from '@/api/checklistApi';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import { canCreateChecklist, getPermissionDeniedMessage } from '@/utils/permissions';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import {
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

// 카테고리 표시 이름 매핑
const CATEGORY_DISPLAY_NAMES = {
  'PREPARATION': '준비물',
  'RESERVATION': '예약',
  'PRE_WORK': '사전 작업',
  'ETC': '기타',
} as const;

// 드래그 가능한 체크리스트 아이템 컴포넌트
interface SortableItemProps {
  item: CheckListItem;
  availableMembers: ClubMember[];
  onUpdateItemContent: (itemId: number, content: string) => void;
  onUpdateItemCategory: (itemId: number, category: string) => void;
  onDeleteItem: (itemId: number) => void;
  onAddMember: (itemId: number, member: ClubMember) => void;
  onRemoveMember: (itemId: number, memberId: number) => void;
  onToggleMemberCheck: (itemId: number, assignId: number) => void;
}

function SortableItem({
  item,
  availableMembers,
  onUpdateItemContent,
  onUpdateItemCategory,
  onDeleteItem,
  onAddMember,
  onRemoveMember,
  onToggleMemberCheck,
}: SortableItemProps) {
  const [showMemberDropdown, setShowMemberDropdown] = useState(false);
  const [memberSearchTerm, setMemberSearchTerm] = useState('');
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowMemberDropdown(false);
      }
    };

    if (showMemberDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showMemberDropdown]);

  // 드롭다운이 닫힐 때 검색어 초기화
  useEffect(() => {
    if (!showMemberDropdown) {
      setMemberSearchTerm('');
    }
  }, [showMemberDropdown]);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id || 0 });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="flex items-center gap-3 p-3 border border-gray-100 rounded-lg hover:bg-gray-50 transition-colors"
    >
      {/* 드래그 핸들 */}
      <div
        {...attributes}
        {...listeners}
        className="cursor-grab active:cursor-grabbing text-gray-400 hover:text-gray-600"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8h16M4 16h16" />
        </svg>
      </div>
      
      <div className="flex-1">
        <div className="space-y-3">
          <div className="flex gap-2">
            <select
              value={item.category || 'ETC'}
              onChange={(e) => onUpdateItemCategory(item.id || 0, e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm text-gray-900 bg-white"
            >
              {Object.entries(CATEGORY_DISPLAY_NAMES).map(([key, value]) => (
                <option key={key} value={key}>{value}</option>
              ))}
            </select>
            <input
              type="text"
              value={item.content || ''}
              onChange={(e) => onUpdateItemContent(item.id || 0, e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 bg-white"
              placeholder="할 일을 입력하세요"
            />
            <button
              onClick={() => onDeleteItem(item.id || 0)}
              className="px-3 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
              title="삭제"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
          
          {/* 담당자 표시 및 관리 */}
          <div className="flex items-center gap-2">
            <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-4.5a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            <div className="flex flex-wrap gap-1">
              {item.itemAssigns?.map((assign) => (
                <span
                  key={assign.id}
                  className={`px-3 py-1 text-sm rounded-full flex items-center gap-1 font-medium ${
                    assign.isChecked
                      ? 'bg-green-100 text-green-800 border border-green-200'
                      : 'bg-gray-100 text-gray-800 border border-gray-200'
                  }`}
                >
                  {/* 멤버 체크박스 */}
                  <button
                    onClick={() => onToggleMemberCheck(item.id || 0, assign.id || 0)}
                    className={`w-4 h-4 rounded border flex items-center justify-center transition-colors ${
                      assign.isChecked
                        ? 'bg-green-500 border-green-500 text-white'
                        : 'border-gray-400 hover:border-green-500'
                    }`}
                    title={assign.isChecked ? '완료 해제' : '완료 체크'}
                  >
                    {assign.isChecked && (
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                  </button>
                  
                  <span className="text-gray-900">{assign.clubMemberName}</span>
                  
                  <button
                    onClick={() => onRemoveMember(item.id || 0, assign.id || 0)}
                    className="ml-1 text-red-500 hover:text-red-700 font-bold"
                    title="담당자 제거"
                  >
                    ×
                  </button>
                </span>
              ))}
              
              {/* 멤버 추가 버튼 */}
              <div className="relative" ref={dropdownRef}>
                <button
                  onClick={() => setShowMemberDropdown(!showMemberDropdown)}
                  className="px-3 py-1 text-sm rounded-full bg-blue-500 text-white hover:bg-blue-600 transition-colors font-medium border border-blue-600 flex items-center gap-1"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  담당자 추가
                </button>
                
                {/* 멤버 선택 드롭다운 */}
                {showMemberDropdown && (
                  <div className="absolute top-full left-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-xl z-20 min-w-48 max-w-64">
                    {/* 검색 입력 */}
                    <div className="p-2 border-b border-gray-100">
                      <input
                        type="text"
                        placeholder="멤버 검색..."
                        value={memberSearchTerm}
                        onChange={(e) => setMemberSearchTerm(e.target.value)}
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 bg-white placeholder-gray-500"
                        autoFocus
                      />
                    </div>
                    
                    {/* 멤버 리스트 */}
                    <div className="max-h-48 overflow-y-auto">
                      {(() => {
                        const availableToAdd = availableMembers
                          .filter(member => 
                            !item.itemAssigns?.some(assign => assign.id === member.clubMemberId)
                          )
                          .filter(member => 
                            (member.nickname || member.name)?.toLowerCase().includes(memberSearchTerm.toLowerCase()) ?? false
                          );
                        
                        if (availableToAdd.length === 0) {
                          return (
                            <div className="px-4 py-3 text-sm text-gray-500 text-center">
                              {memberSearchTerm ? '검색 결과가 없습니다' : '추가할 수 있는 멤버가 없습니다'}
                            </div>
                          );
                        }
                        
                        return availableToAdd.map(member => (
                          <button
                            key={member.id}
                            onClick={() => {
                              onAddMember(item.id || 0, member);
                              setShowMemberDropdown(false);
                            }}
                            className="w-full px-4 py-3 text-left text-sm hover:bg-gray-50 transition-colors flex items-center justify-between group"
                          >
                            <span className="font-medium text-gray-900">{member.nickname || member.name}</span>
                            <svg className="w-4 h-4 text-gray-400 group-hover:text-blue-500 opacity-0 group-hover:opacity-100 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                          </button>
                        ));
                      })()}
                    </div>
                    
                    {/* 푸터 */}
                    <div className="p-2 border-t border-gray-100 bg-gray-50 rounded-b-lg">
                      <div className="text-xs text-gray-500 text-center">
                        {(() => {
                          const totalAvailable = availableMembers.filter(member => 
                            !item.itemAssigns?.some(assign => assign.id === member.clubMemberId)
                          ).length;
                          const filteredCount = availableMembers
                            .filter(member => 
                              !item.itemAssigns?.some(assign => assign.id === member.clubMemberId)
                            )
                            .filter(member => 
                              (member.nickname || member.name)?.toLowerCase().includes(memberSearchTerm.toLowerCase()) ?? false
                            ).length;
                          
                          if (memberSearchTerm) {
                            return `검색 결과 ${filteredCount}명 (전체 ${totalAvailable}명)`;
                          }
                          return `${totalAvailable}명의 멤버를 추가할 수 있습니다`;
                        })()}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function CreateChecklistPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const clubId = searchParams.get('clubId');
  const scheduleId = searchParams.get('scheduleId');
  
  const [checkListItems, setCheckListItems] = useState<CheckListItem[]>([]);
  const [availableMembers, setAvailableMembers] = useState<ClubMember[]>([]);
  const [loading, setLoading] = useState(false);
  const [newItemContent, setNewItemContent] = useState('');
  const [newItemCategory, setNewItemCategory] = useState<keyof typeof CATEGORY_DISPLAY_NAMES>('ETC');
  const [userInfo, setUserInfo] = useState<GroupUserInfo | null>(null);
  const [permissionLoading, setPermissionLoading] = useState(true);
  const [scheduleInfo, setScheduleInfo] = useState<ScheduleInfo | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const loadScheduleInfo = async () => {
    if (!scheduleId) return;
    
    try {
      const response = await fetchScheduleDetail(scheduleId);
      setScheduleInfo(response.data);
      
      // 일정 정보 로드 후 해당 클럽의 멤버들과 사용자 권한 정보를 불러옴
      if (response.data?.clubId) {
        await Promise.all([
          loadGroupMembers(response.data.clubId),
          loadGroupUserInfo(response.data.clubId)
        ]);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      if (errorMessage.startsWith('LOGIN_REQUIRED:')) {
        // 데모 일정 정보
        const demoSchedule: ScheduleInfo = {
          id: parseInt(scheduleId),
          title: '데모 일정',
          startDate: new Date().toISOString(),
          endDate: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
          clubId: 1
        };
        setScheduleInfo(demoSchedule);
        await Promise.all([
          loadGroupMembers(1),
          loadGroupUserInfo(1)
        ]);
      } else {
        console.error('일정 정보를 불러오는 중 오류가 발생했습니다:', errorMessage);
        toast.error('일정 정보를 불러올 수 없습니다.');
      }
    }
  };

  const loadGroupUserInfo = async (clubId?: number) => {
    try {
      // 일정에서 가져온 clubId 사용
      const clubIdToUse = clubId || scheduleInfo?.clubId || 1;
      const response = await fetchGroupUserInfo(clubIdToUse.toString());
      setUserInfo(response.data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      console.error('사용자 권한 정보를 불러오는 중 오류가 발생했습니다:', errorMessage);
      setUserInfo(null);
    } finally {
      setPermissionLoading(false);
    }
  };

  const loadGroupMembers = async (clubId?: number) => {
    try {
      // 임시 clubId (실제로는 일정에서 clubId를 가져와야 함)
      const clubIdToUse = clubId || 1;
      const response = await fetchGroupMembers(clubIdToUse.toString());
      setAvailableMembers(response.data?.members || []);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      console.error('멤버를 불러오는 중 오류가 발생했습니다:', errorMessage);
      setAvailableMembers([]);
    }
  };

  useEffect(() => {
    // clubId나 scheduleId가 없으면 리다이렉트
    if (!clubId) {
      router.replace('/');
      return;
    }
    if (!scheduleId) {
      router.replace(`/schedule?clubId=${clubId}`);
      return;
    }
    
    // 일정 정보 로드 (멤버와 권한 정보는 자동으로 로드됨)
    loadScheduleInfo();
  }, [clubId, scheduleId, router]);

  const handleAddItem = () => {
    if (!newItemContent.trim()) return;
    
    const newItem: CheckListItem = {
      id: Date.now(),
      content: newItemContent.trim(),
      category: newItemCategory,
      sequence: checkListItems.length + 1,
      isChecked: false,
      itemAssigns: []
    };
    
    setCheckListItems([...checkListItems, newItem]);
    setNewItemContent('');
    // 카테고리는 유지하고 입력 모드도 유지
    
    // 입력 필드에 다시 포커스
    setTimeout(() => {
      const inputElement = document.querySelector('input[placeholder*="새 할 일을 입력하고"]') as HTMLInputElement;
      if (inputElement) {
        inputElement.focus();
      }
    }, 50);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      e.stopPropagation();
      handleAddItem();
    }
  };

  const handleUpdateItemContent = (itemId: number, newContent: string) => {
    setCheckListItems(items =>
      items.map(item =>
        item.id === itemId ? { ...item, content: newContent } : item
      )
    );
  };

  const handleUpdateItemCategory = (itemId: number, newCategory: string) => {
    setCheckListItems(items =>
      items.map(item =>
        item.id === itemId ? { ...item, category: newCategory as any } : item
      )
    );
  };

  const handleDeleteItem = (itemId: number) => {
    if (confirm('정말로 이 아이템을 삭제하시겠습니까?')) {
      setCheckListItems(items => items.filter(item => item.id !== itemId));
    }
  };

  const handleAddMember = (itemId: number, member: ClubMember) => {
    setCheckListItems(items =>
      items.map(item => {
        if (item.id === itemId) {
          const newAssign = {
            id: member.clubMemberId, // 담당자의 clubMemberId 사용
            clubMemberName: member.nickname || member.name || '',
            isChecked: false
          };
          
          return {
            ...item,
            itemAssigns: [...(item.itemAssigns || []), newAssign]
          };
        }
        return item;
      })
    );
  };

  const handleRemoveMember = (itemId: number, assignId: number) => {
    setCheckListItems(items =>
      items.map(item => {
        if (item.id === itemId) {
          return {
            ...item,
            itemAssigns: item.itemAssigns?.filter(assign => assign.id !== assignId) || []
          };
        }
        return item;
      })
    );
  };

  const handleToggleMemberCheck = (itemId: number, assignId: number) => {
    setCheckListItems(items =>
      items.map(item => {
        if (item.id === itemId) {
          return {
            ...item,
            itemAssigns: item.itemAssigns?.map(assign => {
              if (assign.id === assignId) {
                return {
                  ...assign,
                  isChecked: !assign.isChecked
                };
              }
              return assign;
            }) || []
          };
        }
        return item;
      })
    );
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over) return;

    const activeId = active.id as number;
    const overId = over.id as number;

    if (activeId === overId) return;

    const oldIndex = checkListItems.findIndex(item => item.id === activeId);
    const newIndex = checkListItems.findIndex(item => item.id === overId);

    if (oldIndex === -1 || newIndex === -1) return;

    // 같은 카테고리 내에서만 이동 허용
    const activeItem = checkListItems[oldIndex];
    const overItem = checkListItems[newIndex];
    
    if (activeItem.category !== overItem.category) return;

    const newItems = arrayMove(checkListItems, oldIndex, newIndex);
    
    // sequence 재정렬
    const updatedItems = newItems.map((item, index) => ({
      ...item,
      sequence: index + 1
    }));

    setCheckListItems(updatedItems);
  };

  const handleSubmit = async () => {
    if (!scheduleId) {
      toast.error('일정 ID가 필요합니다.');
      return;
    }

    if (checkListItems.length === 0) {
      toast.error('최소 하나의 체크리스트 아이템을 추가해주세요.');
      return;
    }

    try {
      setLoading(true);
      
      const checklistData: CheckListWriteReqDto = {
        title: scheduleInfo?.title ? `${scheduleInfo.title} 체크리스트` : `일정 ${scheduleId}의 체크리스트`,
        startDate: scheduleInfo?.startDate || new Date().toISOString(),
        endDate: scheduleInfo?.endDate || new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
        clubId: scheduleInfo?.clubId || 1, // 일정에서 가져온 클럽 ID 사용
        scheduleId: parseInt(scheduleId), // URL에서 받은 일정 ID 사용
        checkListItems: checkListItems.map(item => ({
          content: item.content || '',
          category: item.category || 'ETC',
          sequence: item.sequence || 0,
          itemAssigns: item.itemAssigns?.map(assign => ({
            clubMemberId: assign.id, // clubMemberId 필드명으로 전송
            isChecked: assign.isChecked || false
          })) || []
        }))
      };
      
      const response = await createChecklist(checklistData);
      toast.success('체크리스트가 성공적으로 생성되었습니다.');
      router.push(`/checklists?clubId=${clubId}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      if (errorMessage.startsWith('LOGIN_REQUIRED:')) {
        toast.success('체크리스트가 생성되었습니다. (데모 모드)');
        router.push(`/checklists?clubId=${clubId}`);
      } else if (errorMessage.startsWith('SCHEDULE_NOT_FOUND:')) {
        toast.error('일정을 찾을 수 없습니다. 올바른 일정 ID인지 확인해주세요.');
      } else if (errorMessage.startsWith('PERMISSION_DENIED:')) {
        toast.error('권한이 없습니다. 클럽 관리자 또는 호스트만 체크리스트를 생성할 수 있습니다.');
      } else if (errorMessage.startsWith('ACCESS_DENIED:')) {
        toast.error('이 그룹에 접근할 권한이 없습니다. 그룹 관리자에게 문의하세요.');
      } else if (errorMessage.startsWith('CHECKLIST_ALREADY_EXISTS:')) {
        toast.error('이미 이 일정에 체크리스트가 존재합니다.');
      } else {
        console.error('체크리스트 생성 오류:', errorMessage);
        toast.error('체크리스트 생성 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 카테고리별로 아이템 그룹화
  const groupedItems = checkListItems.reduce((groups, item) => {
    const category = item.category || 'ETC';
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(item);
    return groups;
  }, {} as Record<string, CheckListItem[]>);

  // 카테고리 순서 정의
  const categoryOrder = ['PREPARATION', 'RESERVATION', 'PRE_WORK', 'ETC'] as const;

  // clubId나 scheduleId가 없으면 리다이렉트 중이므로 아무것도 렌더링하지 않음
  if (!clubId || !scheduleId) {
    return null;
  }

  // 권한 정보 로딩 중
  if (permissionLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  // 권한 확인
  if (!canCreateChecklist(userInfo)) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="bg-white rounded-lg border border-red-200 shadow-sm p-8 text-center">
            <div className="text-red-500 mb-4">
              <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">접근 권한이 없습니다</h2>
            <p className="text-gray-600 mb-6">{getPermissionDeniedMessage(userInfo, '체크리스트 생성')}</p>
            <div className="flex gap-3 justify-center">
              <Link
                href={`/checklists?clubId=${clubId}`}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                체크리스트 목록으로
              </Link>
              <button
                onClick={() => window.history.back()}
                className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                이전 페이지로
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

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
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <Link
                href={`/checklists?clubId=${clubId}`}
                className="text-gray-600 hover:text-gray-800 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </Link>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">체크리스트 아이템 추가</h1>
                {scheduleId && (
                  <p className="text-sm text-blue-600 mt-1">일정 ID: {scheduleId}</p>
                )}
              </div>
            </div>
            
            {/* 액션 버튼들 */}
            <div className="flex gap-2">
              <button
                onClick={handleSubmit}
                disabled={loading || checkListItems.length === 0}
                className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? '생성 중...' : '완료'}
              </button>
              <Link
                href={`/schedule?clubId=${clubId}`}
                className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                취소
              </Link>
            </div>
          </div>
          <p className="text-gray-600">일정에 추가할 체크리스트 아이템들을 만들어보세요</p>
        </div>

        {/* 아이템 추가 */}
        <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm mb-6">
          <div className="mb-4">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">체크리스트 아이템 추가</h2>
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="space-y-3">
                <div className="flex items-center gap-2 mb-2">
                  <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  <span className="text-sm font-medium text-blue-700">새 아이템 추가</span>
                  <span className="text-xs text-blue-600">(Enter로 빠르게 추가)</span>
                </div>
                <div className="flex gap-2">
                  <select
                    value={newItemCategory}
                    onChange={(e) => setNewItemCategory(e.target.value as keyof typeof CATEGORY_DISPLAY_NAMES)}
                    className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm text-gray-900 bg-white"
                  >
                    {Object.entries(CATEGORY_DISPLAY_NAMES).map(([key, value]) => (
                      <option key={key} value={key}>{value}</option>
                    ))}
                  </select>
                  <input
                    type="text"
                    value={newItemContent}
                    onChange={(e) => setNewItemContent(e.target.value)}
                    onKeyPress={handleKeyPress}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 bg-white placeholder-gray-500"
                    placeholder="새 할 일을 입력하고 Enter를 누르세요"
                  />
                  <button
                    onClick={handleAddItem}
                    disabled={!newItemContent.trim()}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center gap-1"
                    title="또는 Enter 키를 누르세요"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    추가
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 체크리스트 아이템들 (카테고리별로 그룹화) */}
        <div className="space-y-6">
          {categoryOrder.map(category => {
            const categoryItems = groupedItems[category];
            if (!categoryItems || categoryItems.length === 0) return null;

            return (
              <div key={category} className="bg-white rounded-lg border border-gray-200 shadow-sm">
                <div className="px-4 py-3 border-b border-gray-200 bg-gray-50 rounded-t-lg">
                  <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                    <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                    {CATEGORY_DISPLAY_NAMES[category]}
                    <span className="text-sm font-normal text-gray-500">
                      ({categoryItems.length})
                    </span>
                  </h3>
                </div>
                
                <div className="p-4">
                  <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    onDragEnd={handleDragEnd}
                  >
                    <SortableContext
                      items={categoryItems.map(item => item.id || 0)}
                      strategy={verticalListSortingStrategy}
                    >
                      <div className="space-y-3">
                        {categoryItems
                          .sort((a, b) => (a.sequence || 0) - (b.sequence || 0))
                          .map(item => (
                            <SortableItem
                              key={item.id}
                              item={item}
                              availableMembers={availableMembers}
                              onUpdateItemContent={handleUpdateItemContent}
                              onUpdateItemCategory={handleUpdateItemCategory}
                              onDeleteItem={handleDeleteItem}
                              onAddMember={handleAddMember}
                              onRemoveMember={handleRemoveMember}
                              onToggleMemberCheck={handleToggleMemberCheck}
                            />
                          ))}
                      </div>
                    </SortableContext>
                  </DndContext>
                </div>
              </div>
            );
          })}
        </div>

        {/* 빈 상태 */}
        {checkListItems.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <svg className="w-24 h-24 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 6h6m-6 4h6" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">체크리스트 아이템이 없습니다</h3>
            <p className="text-gray-600 mb-4">아이템 추가 버튼을 클릭하여 할 일을 추가해보세요.</p>
            <p className="text-gray-500">위의 아이템 추가 폼을 사용해서 할 일을 추가해보세요.</p>
          </div>
        )}
      </div>
    </div>
  );
}