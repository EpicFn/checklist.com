// YYYY-mm-dd 포맷
export function formatDateString(dateStr: string): string {
  return dateStr.split('T')[0];
}

// ISO 8601 날짜 문자열에서 날짜 부분만 추출 (YYYY-MM-DD)
export function extractDateFromISO(dateStr: string): string {
  if (!dateStr || typeof dateStr !== 'string') {
    throw new Error('유효하지 않은 날짜 문자열입니다.');
  }
  
  const datePart = dateStr.split('T')[0];
  
  // 기본적인 날짜 형식 검증
  if (!/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
    throw new Error('올바르지 않은 날짜 형식입니다. YYYY-MM-DD 형식이어야 합니다.');
  }
  
  return datePart;
}