'use client';

import { useState, useEffect, useRef } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { CheckList, CheckListItem, CheckListUpdateReqDto } from '@/types/checklist';
import { fetchChecklistDetail, updateChecklist, deleteChecklist, fetchGroupMembers, ClubMember, fetchGroupUserInfo, GroupUserInfo } from '@/api/checklistApi';
import LoadingSpinner from '@/components/global/LoadingSpinner';
import { canEditChecklist, canDeleteChecklist, canViewChecklist, getPermissionDeniedMessage } from '@/utils/permissions';
import toast from 'react-hot-toast';
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
  isEditMode: boolean;
  availableMembers: ClubMember[];
  onToggleItem: (itemId: number) => void;
  onUpdateItemContent: (itemId: number, content: string) => void;
  onUpdateItemCategory: (itemId: number, category: string) => void;
  onDeleteItem: (itemId: number) => void;
  onAddMember: (itemId: number, member: ClubMember) => void;
  onRemoveMember: (itemId: number, memberId: number) => void;
  onToggleMemberCheck: (itemId: number, assignId: number) => void;
}

function SortableItem({
  item,
  isEditMode,
  availableMembers,
  onToggleItem,
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
      {/* 드래그 핸들 (수정 모드에서만 표시) */}
      {isEditMode && (
        <div
          {...attributes}
          {...listeners}
          className="cursor-grab active:cursor-grabbing text-gray-400 hover:text-gray-600"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8h16M4 16h16" />
          </svg>
        </div>
      )}

      {/* 체크박스 */}
      {isEditMode ? (
        <button
          onClick={() => onToggleItem(item.id || 0)}
          className={`w-5 h-5 rounded border-2 flex items-center justify-center transition-colors ${
            item.isChecked
              ? 'bg-green-500 border-green-500 text-white'
              : 'border-gray-300 hover:border-green-400'
          }`}
        >
          {item.isChecked && (
            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
            </svg>
          )}
        </button>
      ) : (
        <div
          className={`w-5 h-5 rounded border-2 flex items-center justify-center ${
            item.isChecked
              ? 'bg-green-500 border-green-500 text-white'
              : 'border-gray-300'
          }`}
        >
          {item.isChecked && (
            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
            </svg>
          )}
        </div>
      )}
      
      <div className="flex-1">
        {isEditMode ? (
          /* 수정 모드 */
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
                    {/* 멤버 체크박스 (수정 모드에서만 클릭 가능) */}
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
        ) : (
          /* 보기 모드 */
          <>
            <span className={`${
              item.isChecked ? 'text-gray-500 line-through' : 'text-gray-900'
            }`}>
              {item.content}
            </span>
            
            {/* 담당자 표시 */}
            {item.itemAssigns && item.itemAssigns.length > 0 && (
              <div className="flex items-center gap-2 mt-2">
                <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-4.5a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                <div className="flex flex-wrap gap-1">
                  {item.itemAssigns.map((assign) => (
                    <span
                      key={assign.id}
                      className={`px-3 py-1 text-sm rounded-full flex items-center gap-1 font-medium ${
                        assign.isChecked
                          ? 'bg-green-100 text-green-800 border border-green-200'
                          : 'bg-gray-100 text-gray-800 border border-gray-200'
                      }`}
                    >
                      <span className="text-gray-900">{assign.clubMemberName}</span>
                      {assign.isChecked && (
                        <svg className="w-3 h-3 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                        </svg>
                      )}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default function ChecklistDetailPage() {
  const params = useParams();
  const router = useRouter();
  const searchParams = useSearchParams();
  const checklistId = params.checklistId as string;
  const clubId = searchParams.get('clubId');

  // 서버 데이터를 클라이언트 형식으로 변환하는 함수
  const convertServerDataToClient = (checklistData: CheckList | null): CheckList | null => {
    if (!checklistData?.checkListItems) return checklistData;
    
    return {
      ...checklistData,
      checkListItems: checklistData.checkListItems.map(item => ({
        ...item,
        itemAssigns: item.itemAssigns?.map(assign => ({
          ...assign,
          id: assign.clubMemberId || assign.id, // clubMemberId를 id로 사용
        })) || []
      }))
    };
  };
  
  const [checklist, setChecklist] = useState<CheckList | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditMode, setIsEditMode] = useState(false);
  const [saving, setSaving] = useState(false);
  const [availableMembers, setAvailableMembers] = useState<ClubMember[]>([]);
  const [userInfo, setUserInfo] = useState<GroupUserInfo | null>(null);
  const [permissionLoading, setPermissionLoading] = useState(true);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const loadChecklistDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetchChecklistDetail(checklistId);
      const checklistData = convertServerDataToClient(response.data || null);
      setChecklist(checklistData);
      
      // 체크리스트 로드 후 해당 클럽의 멤버들과 사용자 권한 정보를 불러옴
      if (checklistData?.schedule?.clubId) {
        await Promise.all([
          loadGroupMembers(checklistData.schedule.clubId),
          loadGroupUserInfo(checklistData.schedule.clubId)
        ]);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      if (errorMessage.startsWith('ACCESS_DENIED:')) {
        setError('이 체크리스트에 접근할 권한이 없습니다. 그룹 관리자에게 문의하세요.');
      } else {
        setError('체크리스트를 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const loadGroupUserInfo = async (clubId?: number) => {
    try {
      // 체크리스트에서 clubId를 가져와서 사용
      const clubIdToUse = clubId || checklist?.schedule?.clubId || 1;
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
      // 체크리스트에서 clubId를 가져와서 사용
      const clubIdToUse = clubId || checklist?.schedule?.clubId || 1;
      const response = await fetchGroupMembers(clubIdToUse.toString());
      setAvailableMembers(response.data?.members || []);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      console.error('멤버를 불러오는 중 오류가 발생했습니다:', errorMessage);
      setAvailableMembers([]);
    }
  };

  useEffect(() => {
    if (!clubId) {
      setLoading(false);
      setError('클럽 ID가 필요합니다. 올바른 클럽 페이지에서 접근해주세요.');
      return;
    }
    
    if (checklistId) {
      loadChecklistDetail();
    }
  }, [checklistId, clubId]);

  const handleToggleItem = (itemId: number) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item =>
        item.id === itemId ? { ...item, isChecked: !item.isChecked } : item
      ),
    });
  };

  const handleEditChecklist = () => {
    setIsEditMode(true);
  };

  const handleSaveChecklist = async () => {
    if (!checklist) return;
    
    try {
      setSaving(true);
      
      // 체크리스트 업데이트 데이터 준비
      const updateData: CheckListUpdateReqDto = {
        checkListItems: checklist.checkListItems?.map(item => ({
          id: item.id,
          content: item.content || '',
          category: item.category || 'ETC',
          sequence: item.sequence || 0,
          isChecked: item.isChecked || false,
          itemAssigns: item.itemAssigns?.map(assign => ({
            clubMemberId: assign.id, // clubMemberId 필드명으로 전송
            isChecked: assign.isChecked || false
          })) || []
        })) || []
      };
      
      const response = await updateChecklist(checklistId, updateData);
      const updatedData = convertServerDataToClient(response.data || checklist);
      setChecklist(updatedData);
      setIsEditMode(false);
      toast.success('체크리스트가 성공적으로 수정되었습니다.');
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      
      if (errorMessage.startsWith('ACCESS_DENIED:')) {
        toast.error('이 체크리스트를 수정할 권한이 없습니다. 그룹 관리자에게 문의하세요.');
      } else {
        toast.error('체크리스트 수정 중 오류가 발생했습니다.');
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCancelEdit = () => {
    setIsEditMode(false);
    // 원본 데이터로 복원
    loadChecklistDetail();
  };

  const handleDeleteChecklist = async () => {
    if (!checklist) return;

    if (confirm('정말로 이 체크리스트를 삭제하시겠습니까?')) {
      try {
        await deleteChecklist(checklistId);
        toast.success('체크리스트가 성공적으로 삭제되었습니다.');
        router.push(`/checklists?clubId=${clubId}`);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
        
        if (errorMessage.startsWith('ACCESS_DENIED:')) {
          toast.error('이 체크리스트를 삭제할 권한이 없습니다. 그룹 관리자에게 문의하세요.');
        } else {
          toast.error('체크리스트 삭제 중 오류가 발생했습니다.');
        }
      }
    }
  };

  const handleUpdateItemContent = (itemId: number, newContent: string) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item =>
        item.id === itemId ? { ...item, content: newContent } : item
      ),
    });
  };

  const handleUpdateItemCategory = (itemId: number, newCategory: string) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item =>
        item.id === itemId ? { ...item, category: newCategory as any } : item
      ),
    });
  };

  const handleDeleteItem = (itemId: number) => {
    if (!checklist) return;
    
    if (confirm('정말로 이 아이템을 삭제하시겠습니까?')) {
      setChecklist({
        ...checklist,
        checkListItems: checklist.checkListItems?.filter(item => item.id !== itemId) || []
      });
    }
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || !checklist || !checklist.checkListItems) return;

    const activeId = active.id as number;
    const overId = over.id as number;

    if (activeId === overId) return;

    const items = checklist.checkListItems;
    const oldIndex = items.findIndex(item => item.id === activeId);
    const newIndex = items.findIndex(item => item.id === overId);

    if (oldIndex === -1 || newIndex === -1) return;

    // 같은 카테고리 내에서만 이동 허용
    const activeItem = items[oldIndex];
    const overItem = items[newIndex];
    
    if (activeItem.category !== overItem.category) return;

    const newItems = arrayMove(items, oldIndex, newIndex);
    
    // sequence 재정렬
    const updatedItems = newItems.map((item, index) => ({
      ...item,
      sequence: index + 1
    }));

    setChecklist({
      ...checklist,
      checkListItems: updatedItems
    });
  };

  const handleAddMember = (itemId: number, member: ClubMember) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item => {
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
      }) || []
    });
  };

  const handleRemoveMember = (itemId: number, assignId: number) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item => {
        if (item.id === itemId) {
          return {
            ...item,
            itemAssigns: item.itemAssigns?.filter(assign => assign.id !== assignId) || []
          };
        }
        return item;
      }) || []
    });
  };

  const handleToggleMemberCheck = (itemId: number, assignId: number) => {
    if (!checklist) return;
    
    setChecklist({
      ...checklist,
      checkListItems: checklist.checkListItems?.map(item => {
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
      }) || []
    });
  };

  const handleAddItem = (category: string) => {
    if (!checklist) return;
    
    // 해당 카테고리의 다음 sequence 계산
    const categoryItems = checklist.checkListItems?.filter(item => item.category === category) || [];
    const maxSequence = categoryItems.length > 0 ? Math.max(...categoryItems.map(item => item.sequence || 0)) : 0;
    
    // 전체 아이템의 다음 ID 계산 (임시 ID로 음수 사용)
    const existingIds = checklist.checkListItems?.map(item => item.id || 0) || [];
    const minId = existingIds.length > 0 ? Math.min(...existingIds) : 0;
    const newId = minId <= 0 ? minId - 1 : -1;
    
    const newItem: CheckListItem = {
      id: newId,
      content: '',
      category: category as any,
      sequence: maxSequence + 1,
      isChecked: false,
      itemAssigns: []
    };
    
    setChecklist({
      ...checklist,
      checkListItems: [...(checklist.checkListItems || []), newItem]
    });
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatDateRange = (startDate: string, endDate: string) => {
    if (startDate === endDate) {
      return formatDate(startDate);
    }
    return `${formatDate(startDate)} - ${formatDate(endDate)}`;
  };

  // 카테고리별로 아이템 그룹화
  const groupedItems = checklist?.checkListItems?.reduce((groups, item) => {
    const category = item.category || 'ETC';
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(item);
    return groups;
  }, {} as Record<string, CheckListItem[]>) || {};

  // 카테고리 순서 정의
  const categoryOrder = ['PREPARATION', 'RESERVATION', 'PRE_WORK', 'ETC'] as const;

  if (loading || permissionLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  // 권한 확인
  if (!canViewChecklist(userInfo)) {
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
            <p className="text-gray-600 mb-6">{getPermissionDeniedMessage(userInfo, '체크리스트 조회')}</p>
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

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="bg-white rounded-lg border border-red-200 shadow-sm p-8 text-center">
            <div className="text-red-500 mb-4">
              <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">오류가 발생했습니다</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <div className="flex gap-3 justify-center">
              {clubId ? (
                <Link
                  href={`/checklists?clubId=${clubId}`}
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  체크리스트 목록으로
                </Link>
              ) : (
                <Link
                  href="/"
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  홈으로 이동
                </Link>
              )}
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

  if (!checklist) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="text-center">
            <h2 className="text-xl font-semibold text-gray-800 mb-2">체크리스트를 찾을 수 없습니다</h2>
            <Link
              href={`/checklists?groupId=${groupId}`}
              className="text-blue-500 hover:text-blue-600 underline"
            >
              체크리스트 목록으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const totalItems = checklist.checkListItems?.length || 0;
  const completedItems = checklist.checkListItems?.filter(item => item.isChecked).length || 0;
  const progressPercentage = totalItems > 0 ? Math.round((completedItems / totalItems) * 100) : 0;

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
              <h1 className="text-3xl font-bold text-gray-900">{checklist.schedule?.title || '체크리스트'}</h1>
            </div>
            
            {/* 액션 버튼들 */}
            <div className="flex gap-2">
              {isEditMode ? (
                <>
                  <button
                    onClick={handleSaveChecklist}
                    disabled={saving}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                  >
                    {saving ? '저장 중...' : '저장'}
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
                  {canEditChecklist(userInfo) && (
                    <button
                      onClick={handleEditChecklist}
                      className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                    >
                      수정
                    </button>
                  )}
                </>
              )}
            </div>
          </div>
          {isEditMode && (
            <p className="text-gray-600 text-sm mt-2">체크리스트 아이템들을 수정할 수 있습니다</p>
          )}
          {!canEditChecklist(userInfo) && userInfo?.state === 'JOINING' && (
            <p className="text-amber-600 text-sm mt-2 bg-amber-50 px-3 py-2 rounded-lg border border-amber-200">
              ℹ️ 체크리스트 수정은 클럽 관리자(호스트/매니저)만 가능합니다
            </p>
          )}
          
          {/* 일정 정보 */}
          {checklist.schedule && (
            <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm mb-6">
              <div className="flex items-center gap-4 text-sm text-gray-600 mb-3">
                <div className="flex items-center gap-1">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3a1 1 0 012 0v4h4V3a1 1 0 012 0v4h.586l.707.707c.39.39.586.902.586 1.414v11.172a2 2 0 01-.586 1.414l-.707.707H7.414l-.707-.707A2 2 0 016 18.586V7.414c0-.512.196-1.024.586-1.414L7.293 5.293A1 1 0 018 5z" />
                  </svg>
                  <span>{formatDateRange(checklist.schedule.startDate || '', checklist.schedule.endDate || '')}</span>
                </div>
                
                {checklist.schedule.spot && (
                  <div className="flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <span>{checklist.schedule.spot}</span>
                  </div>
                )}
              </div>
              
              {/* 진행률 */}
              <div className="mb-3">
                <div className="flex items-center justify-between text-sm text-gray-600 mb-2">
                  <span>진행률</span>
                  <span>{completedItems}/{totalItems} 완료 ({progressPercentage}%)</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-green-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${progressPercentage}%` }}
                  ></div>
                </div>
              </div>
            </div>
          )}
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
                      ({categoryItems.filter(item => item.isChecked).length}/{categoryItems.length})
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
                              isEditMode={isEditMode}
                              availableMembers={availableMembers}
                              onToggleItem={handleToggleItem}
                              onUpdateItemContent={handleUpdateItemContent}
                              onUpdateItemCategory={handleUpdateItemCategory}
                              onDeleteItem={handleDeleteItem}
                              onAddMember={handleAddMember}
                              onRemoveMember={handleRemoveMember}
                              onToggleMemberCheck={handleToggleMemberCheck}
                            />
                          ))}
                        
                        {/* 아이템 추가 버튼 (수정 모드에서만 표시) */}
                        {isEditMode && (
                          <button
                            onClick={() => handleAddItem(category)}
                            className="w-full p-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-blue-400 hover:text-blue-600 hover:bg-blue-50 transition-colors flex items-center justify-center gap-2"
                          >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                            <span className="font-medium">새 아이템 추가</span>
                          </button>
                        )}
                      </div>
                    </SortableContext>
                  </DndContext>
                </div>
              </div>
            );
          })}
        </div>

        {/* 빈 상태 */}
        {Object.keys(groupedItems).length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <svg className="w-24 h-24 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 6h6m-6 4h6" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">체크리스트 아이템이 없습니다</h3>
            <p className="text-gray-600">아직 할 일이 추가되지 않았습니다.</p>
          </div>
        )}
      </div>
    </div>
  );
}