'use client';

import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';

import { getFriends, acceptFriend, rejectFriend, deleteFriend, addFriend } from '@/api/friend';
import { FriendDto, FriendStatusDto, FriendStatusMap } from '@/types/friend';
import FriendsItem from '@/components/domain/friend/FriendsItem';
import AccordionPanel from '@/components/domain/friend/FriendsAccordion';

const FriendsList: React.FC = () => {
  // 친구 상태별 목록 처리
  const [acceptedFriends, setAcceptedFriends] = useState<FriendDto[]>([]);
  const [sentFriends, setSentFriends] = useState<FriendDto[]>([]);
  const [receivedFriends, setReceivedFriends] = useState<FriendDto[]>([]);

  // 친구 추가 위한 검색어(이메일)
  const [searchTerm, setSearchTerm] = useState<string>('');

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 아코디언 패널의 열림/닫힘 상태 관리
  const [accordionState, setAccordionState] = useState({
    [FriendStatusDto.ACCEPTED]: true,
    [FriendStatusDto.SENT]: false,
    [FriendStatusDto.RECEIVED]: false,
    [FriendStatusDto.REJECTED]: false,
    [FriendStatusDto.ALL]: false,
  });

  const fetchFriendsData = async () => {
    try {
      // 각 상태별 친구 목록 조회
      const [acceptedRes, sentRes, receivedRes] = await Promise.all([
        getFriends(FriendStatusDto.ACCEPTED),
        getFriends(FriendStatusDto.SENT),
        getFriends(FriendStatusDto.RECEIVED),
      ]);

      // 각 상태별 친구 목록 세팅
      setAcceptedFriends(acceptedRes.data ?? []);
      setSentFriends(sentRes.data ?? []);
      setReceivedFriends(receivedRes.data ?? []);
    } catch (e) {
      const msg = e instanceof Error ? e.message : '친구 목록을 불러오는 데 실패했습니다.';
      setError(msg);
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  // 최초 조회
  useEffect(() => {
    fetchFriendsData();
  }, []);

  // 아코디언 패널 토글
  const handleToggleAccordion = (status: FriendStatusDto) => {
    setAccordionState(prev => ({
      ...prev,
      [status]: !prev[status],
    }));
  };

  // 친구 목록 새로고침
  const refreshFriendsList = () => {
    setIsLoading(true);
    fetchFriendsData();
  };

  // 친구 요청 수락
  const handleAcceptFriend = async (friendId: number) => {
    try {
      const data = await acceptFriend(friendId);
      toast.success(data.message || "친구가 수락되었습니다.");
      refreshFriendsList();
    } catch (e) {
      toast.error(e instanceof Error ? e.message : '친구 수락에 실패했습니다.');
    }
  };

  // 친구 요청 거절
  const handleRejectFriend = async (friendId: number) => {
    if (!confirm('정말 이 친구를 거절하시겠습니까?')) return;

    try {
      const data = await rejectFriend(friendId);
      toast.success(data.message || "친구가 거절되었습니다.");
      refreshFriendsList();
    } catch (e) {
      toast.error(e instanceof Error ? e.message : '친구 거절에 실패했습니다.');
    }
  };

  // 친구 삭제
  const handleDeleteFriend = async (friendId: number) => {
    if (!confirm('정말 이 친구를 삭제하시겠습니까?')) return;

    try {
      const data = await deleteFriend(friendId);
      toast.success(data.message || "친구가 삭제되었습니다.");
      refreshFriendsList();
    } catch (e) {
      toast.error(e instanceof Error ? e.message : '친구 삭제에 실패했습니다.');
    }
  };

  // 친구 추가
  const handleFriendAdd = async () => {
    if (!searchTerm) {
      toast.error("친구의 이메일을 입력해주세요.");
      return;
    }

    try {
      // 친구 추가 요청
      const data = await addFriend(searchTerm);
      toast.success(data.message || `${searchTerm} 님에게 친구 요청을 보냈습니다.`);

      // 검색어 초기화, 친구 목록 재조회
      setSearchTerm('');
      refreshFriendsList();
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : '친구 요청에 실패했습니다.';
      toast.error(errorMessage);
    }
  };

  if (isLoading) {
    return (
      <div className="text-center py-12 text-[22px] text-gray-400 font-semibold tracking-wide min-h-screen bg-gradient-to-br from-[#f8f8f8] via-[#f8f8f8] to-[#e3f2fd]">
        <span role="img" aria-label="loading" className="text-[36px] mr-2.5">⏳</span>
        친구 목록을 불러오는 중...
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12 text-red-500 font-semibold text-[20px] min-h-screen bg-gradient-to-br from-[#f8f8f8] via-[#f8f8f8] to-[#e3f2fd]">
        <span role="img" aria-label="error" className="text-[32px] mr-2.5">😢</span>
        {error}
      </div>
    );
  }

  return (
    <div className="w-screen min-h-screen bg-gradient-to-br from-[#f8f8f8] via-[#f8f8f8] to-[#e3f2fd] box-border flex flex-col items-center">
      <div className="w-[92vw] max-w-[900px] min-w-[380px] mx-auto py-10 pb-7 box-border flex flex-col items-center">
        {/* 타이틀 */}
        <div className="text-center mb-6">
          <span className="inline-block text-[1.35rem] font-extrabold text-[#222] tracking-wide px-3.5 pb-2 text-shadow">
            친구 목록
          </span>
        </div>

        {/* 검색과 친구추가 버튼 */}
        <div className="flex justify-end gap-2 mb-4 items-center w-full max-w-[540px]">
          <input
            type="text"
            placeholder="친구의 이메일을 입력하세요"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 min-w-0 px-2.5 py-1.5 rounded-lg border border-gray-300 text-[15px] text-gray-700 bg-white outline-none font-medium shadow-sm transition-colors h-9"
          />
          <button
            className="px-4 py-1.5 rounded-full bg-blue-500 text-white text-[15px] min-w-[90px] h-9 font-bold tracking-wide shadow transition hover:bg-blue-600"
            onClick={handleFriendAdd}
          >
            친구 요청 보내기
          </button>
        </div>

        {/* 아코디언: 친구인 목록 */}
        <AccordionPanel
          title={<>{FriendStatusMap[FriendStatusDto.ACCEPTED]}</>}
          count={acceptedFriends.length}
          isOpen={accordionState[FriendStatusDto.ACCEPTED]}
          onToggle={() => handleToggleAccordion(FriendStatusDto.ACCEPTED)}
        >
          {acceptedFriends.length > 0 ? (
            acceptedFriends.map((f, idx) => (
              <FriendsItem
                key={f.friendId}
                friend={f}
                status={FriendStatusDto.ACCEPTED}
                idx={idx}
                onDelete={handleDeleteFriend}
                onAccept={handleAcceptFriend}
                onReject={handleRejectFriend}
              />
            ))
          ) : (
            <p className="text-center text-gray-300 text-[14px] m-0 py-2.5">
              <span role="img" aria-label="no-friends">🫥</span> 친구가 없습니다.
            </p>
          )}
        </AccordionPanel>

        {/* 아코디언: 친구 요청 받은 목록 */}
        <AccordionPanel
          title={<>{FriendStatusMap[FriendStatusDto.RECEIVED]}</>}
          count={receivedFriends.length}
          isOpen={accordionState[FriendStatusDto.RECEIVED]}
          onToggle={() => handleToggleAccordion(FriendStatusDto.RECEIVED)}
        >
          {receivedFriends.length > 0 ? (
            receivedFriends.map((f, idx) => (
              <FriendsItem
                key={f.friendId}
                friend={f}
                status={FriendStatusDto.RECEIVED}
                idx={idx}
                onDelete={handleDeleteFriend}
                onAccept={handleAcceptFriend}
                onReject={handleRejectFriend}
              />
            ))
          ) : (
            <p className="text-center text-gray-300 text-[14px] m-0 py-2.5">
              <span role="img" aria-label="no-requests">😶‍🌫️</span> 받은 친구 요청이 없습니다.
            </p>
          )}
        </AccordionPanel>

        {/* 아코디언: 친구 요청한 목록 */}
        <AccordionPanel
          title={<>{FriendStatusMap[FriendStatusDto.SENT]}</>}
          count={sentFriends.length}
          isOpen={accordionState[FriendStatusDto.SENT]}
          onToggle={() => handleToggleAccordion(FriendStatusDto.SENT)}
        >
          {sentFriends.length > 0 ? (
            sentFriends.map((f, idx) => (
              <FriendsItem
                key={f.friendId}
                friend={f}
                status={FriendStatusDto.SENT}
                idx={idx}
                onDelete={handleDeleteFriend}
                onAccept={handleAcceptFriend}
                onReject={handleRejectFriend}
              />
            ))
          ) : (
            <p className="text-center text-gray-300 text-[14px] m-0 py-2.5">
              <span role="img" aria-label="no-sent">🫠</span> 보낸 친구 요청이 없습니다.
            </p>
          )}
        </AccordionPanel>
      </div>
    </div>
  );
};

export default FriendsList;