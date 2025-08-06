import { components } from '@/types/backend/apiV1/schema';

// 백엔드 스키마에서 타입 가져오기
export type PresetDto = components['schemas']['PresetDto'];
export type PresetItemDto = components['schemas']['PresetItemDto'];
export type PresetWriteReqDto = components['schemas']['PresetWriteReqDto'];
export type PresetItemWriteReqDto = components['schemas']['PresetItemWriteReqDto'];
export type RsDataPresetDto = components['schemas']['RsDataPresetDto'];
export type RsDataListPresetDto = components['schemas']['RsDataListPresetDto'];

// 기존 타입과의 호환성을 위한 타입 별칭
export type Preset = PresetDto;
export type PresetItem = PresetItemDto;
export type PresetListResponse = RsDataListPresetDto;

// 카테고리 타입
export type Category = 'PREPARATION' | 'RESERVATION' | 'PRE_WORK' | 'ETC';

// 에러 응답 타입
export interface ApiErrorResponse {
  code: number;
  message: string;
  data: null;
} 