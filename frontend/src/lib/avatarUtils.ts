
// 프로필 이미지 색상 팔레트
const AVATAR_COLORS = [
  '#FFB6B6', '#B6E2FF', '#B6FFB6', '#FFF5B6', '#D1B6FF', '#FFB6E2', '#B6FFD1'
];

// 이름에서 이니셜을 추출
export function getInitials(name: string) {
  if (!name || !name.trim()) return '';
  const parts = name.trim().split(' ').filter(part => part.length > 0);
  if (parts.length === 0) return '';
  if (parts.length === 1) return parts[0][0].toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}

// 인덱스에 따라 아바타 색상을 반환
export function getAvatarColor(idx: number) {
  if (typeof idx !== 'number' || idx < 0) return AVATAR_COLORS[0];
  return AVATAR_COLORS[idx % AVATAR_COLORS.length];
}