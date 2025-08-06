import { components } from '@/types/backend/apiV1/schema';

// 백엔드 스키마에서 타입 가져오기
export type CheckListDto = components['schemas']['CheckListDto'];
export type CheckListItemDto = components['schemas']['CheckListItemDto'];
export type CheckListWriteReqDto = components['schemas']['CheckListWriteReqDto'];
export type CheckListUpdateReqDto = components['schemas']['CheckListUpdateReqDto'];
export type CheckListItemWriteReqDto = components['schemas']['CheckListItemWriteReqDto'];
export type RsDataCheckListDto = components['schemas']['RsDataCheckListDto'];
export type RsDataListCheckListDto = components['schemas']['RsDataListCheckListDto'];

// 기존 타입과의 호환성을 위한 타입 별칭
export type CheckList = CheckListDto;
export type CheckListItem = CheckListItemDto;
export type CheckListResponse = RsDataCheckListDto;
export type CheckListListResponse = RsDataListCheckListDto;

// 에러 응답 타입
export interface ApiErrorResponse {
  code: number;
  message: string;
  data: null;
}