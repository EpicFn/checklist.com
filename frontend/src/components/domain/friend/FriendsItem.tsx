'use client';

import React from 'react';
import { FriendDto, FriendStatusDto } from '@/types/friend';
import { getInitials, getAvatarColor } from '@/lib/avatarUtils';

interface FriendsItemProps {
  friend: FriendDto;
  status: FriendStatusDto;
  idx: number;
  onDelete: (id: number) => void;
  onAccept: (id: number) => void;
  onReject: (id: number) => void;
}

// 친구 아이템
const FriendsItem: React.FC<FriendsItemProps> = ({ friend, status, idx, onDelete, onAccept, onReject }) => {
  if (!friend.friendId || typeof friend.friendId !== 'number' || isNaN(friend.friendId)) return null;
  const avatarColor = getAvatarColor(idx);

  return (
    <div
      key={friend.friendId}
      className="flex items-center p-3 bg-white rounded-xl border border-gray-200 mb-3 shadow-sm gap-4 min-w-0 transition-shadow"
    >
      {friend.friendProfileImageUrl ? (
        <div
          className="w-[54px] h-[54px] rounded-full border-2 shadow-[0_2px_8px_0_rgba(0,0,0,0.07)] bg-[#f3f3f3] flex items-center justify-center overflow-hidden"
          style={{
            borderColor: avatarColor,
          }}
        >
          <img
            src={friend.friendProfileImageUrl}
            alt={`${friend.friendNickname} 프로필`}
            className="w-full h-full object-cover rounded-full"
            style={{
              aspectRatio: "1 / 1",
              width: "100%",
              height: "100%",
              objectFit: "cover",
              display: "block",
            }}
          />
        </div>
      ) : (
        <div
          className={`w-[54px] h-[54px] rounded-full flex items-center justify-center font-bold text-lg text-white border-2 shadow-[0_2px_8px_0_rgba(0,0,0,0.07)] select-none`}
          style={{
            backgroundColor: avatarColor,
          }}
        >
          {getInitials(friend.friendNickname || '')}
        </div>
      )}
      <div className="flex-grow min-w-0">
        <div className="flex items-center gap-2.5">
          <h3 className="m-0 text-[17px] font-bold text-gray-700 truncate max-w-[160px]">
            {friend.friendNickname}
          </h3>
        </div>
        <p className="mt-1 text-[13px] text-gray-400 truncate max-w-[280px]">
          {friend.friendBio}
        </p>
      </div>
      <div className="flex gap-2.5">
        {status === FriendStatusDto.ACCEPTED && (
          <button
            onClick={() => friend.friendId && onDelete(friend.friendId)}
            className="px-4 py-1.5 rounded-full text-white font-semibold text-[14px] min-w-[60px] h-9 bg-red-300 shadow transition hover:bg-red-600"
          >
            삭제
          </button>
        )}
        {status === FriendStatusDto.RECEIVED && (
          <>
            <button
              onClick={() => friend.friendId && onAccept(friend.friendId)}
              className="px-4 py-1.5 rounded-full text-white font-semibold text-[14px] min-w-[60px] h-9 bg-green-400 shadow transition hover:bg-green-700"
            >
              수락
            </button>
            <button
              onClick={() => friend.friendId && onReject(friend.friendId)}
              className="px-4 py-1.5 rounded-full text-white font-semibold text-[14px] min-w-[60px] h-9 bg-gray-400 shadow transition hover:bg-gray-500"
            >
              거절
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default FriendsItem;