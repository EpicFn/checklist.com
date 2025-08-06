import { components } from "@/types/backend/apiV1/schema";

const API_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

type CreateClubRequest = components['schemas']['CreateClubRequest'];
type UpdateClubRequest = components['schemas']['UpdateClubRequest'];
type ClubResponse = components['schemas']['ClubResponse'];
type RsDataPageSimpleClubInfoWithoutLeader = components['schemas']['RsDataPageSimpleClubInfoWithoutLeader'];

/**
 * 모임 생성
 * @param data 모임 데이터
 * @param imageFile 모임 이미지
 * @returns 모임 생성 결과
 */
export const createClub = async (
    data: CreateClubRequest,
    imageFile: File | null,
): Promise<ClubResponse> => {
    const MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB 제한

    // 0. 이미지 크기 검사
    if (imageFile && imageFile.size > MAX_IMAGE_SIZE) {
        throw new Error('이미지 파일 크기는 5MB를 초과할 수 없습니다.');
    }

    // 1. FormData 객체 생성
    const formData = new FormData();

    // 2. JSON 데이터를 Blob으로 변환하여 FormData에 추가
    // 'data'라는 이름(key)으로 JSON 데이터를 추가합니다. 이는 스키마에 정의된 이름입니다.
    formData.append(
        'data',
        new Blob([JSON.stringify(data)], { type: 'application/json' }),
    );

    // 3. 이미지 파일이 있으면 FormData에 추가
    // 'image'라는 이름(key)으로 파일을 추가합니다.
    if (imageFile) {
        formData.append('image', imageFile);
    }

    // 4. fetch API로 요청 전송
    const response = await fetch(`${API_URL}/api/v1/clubs`, {
        method: 'POST',
        // FormData를 body로 사용할 때는 'Content-Type' 헤더를 직접 설정하지 않습니다.
        // 브라우저가 자동으로 'multipart/form-data'와 함께 올바른 boundary를 설정해줍니다.
        body: formData,
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    const responseData = await response.json();

    if (!response.ok) {
        throw new Error(responseData.message || '모임 생성에 실패했습니다.');
    }

    return responseData.data;
};

/**
 * 모임 정보 조회
 * @param clubId 모임 ID
 * @returns 
 */
export const getClubInfo = async (clubId: string): Promise<components['schemas']['RsDataClubInfoResponse']> => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}`, {
        method: 'GET',
        credentials: 'include', // 쿠키를 포함하여 요청
    });


    if (!response.ok) {
        const errorData = await response.json();
        if (!('code' in errorData) || !('message' in errorData)) {
            throw new Error('모임 정보를 가져오는 데 실패했습니다.');
        }
        else {
            throw new Error(errorData.code + " : " + (errorData.message || '모임 정보를 가져오는 데 실패했습니다.'));
        }
    }

    const data: components['schemas']['RsDataClubInfoResponse'] = await response.json();
    return data;
}

/**
 * 모임 정보 수정
 * @param clubId 모임 ID
 * @param data 수정할 모임 데이터
 * @param imageFile 수정할 이미지 파일 (선택적)
 * @returns 수정된 모임 정보
 */
export const updateClub = async (
    clubId: string,
    data: UpdateClubRequest,
    imageFile: File | null = null,
): Promise<ClubResponse> => {
    const MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB 제한

    // 0. 이미지 크기 검사
    if (imageFile && imageFile.size > MAX_IMAGE_SIZE) {
        throw new Error('이미지 파일 크기는 5MB를 초과할 수 없습니다.');
    }

    // 1. FormData 객체 생성
    const formData = new FormData();

    // 2. JSON 데이터를 Blob으로 변환하여 FormData에 추가
    formData.append(
        'data',
        new Blob([JSON.stringify(data)], { type: 'application/json' }),
    );

    // 3. 이미지 파일이 있으면 FormData에 추가
    if (imageFile) {
        formData.append('image', imageFile);
    }

    // 4. fetch API로 요청 전송
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}`, {
        method: 'PATCH',
        body: formData,
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    const responseData = await response.json();

    if (!response.ok) {
        throw new Error(responseData.message || '모임 수정에 실패했습니다.');
    }

    return responseData.data;
}

/**
 * 모임 삭제
 * @param clubId 모임 ID
 * @returns 
 */
export const deleteClub = async (clubId: string): Promise<void> => {
    const response = await fetch(`${API_URL}/api/v1/clubs/${clubId}`, {
        method: 'DELETE',
        credentials: 'include', // 쿠키를 포함하여 요청
    });

    if (!response.ok) {
        const errorData = await response.json();
        if (!('code' in errorData) || !('message' in errorData)) {
            throw new Error('모임 삭제에 실패했습니다.');
        } else {
            throw new Error(errorData.code + " : " + (errorData.message || '모임 삭제에 실패했습니다.'));
        }
    }
}



/**
 * 공개 모임 목록 조회
 * @param page 페이지 번호 (기본값: 0)
 * @param size 페이지 크기 (기본값: 10)
 * @param sort 정렬 기준 (기본값: 'createdAt,desc')
 * @return 공개 모임 목록
 */
export const getPublicClubs = async (
    page: number = 0,
    size: number = 10,
    sort: string = 'id,desc',
    name?: string | null,
    category?: string | null,
    mainSpot?: string | null,
    eventType?: string | null
): Promise<RsDataPageSimpleClubInfoWithoutLeader> => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort: sort
    });

    if (name) params.append('name', name);
    if (category) params.append('category', category);
    if (mainSpot) params.append('mainSpot', mainSpot);
    if (eventType) params.append('eventType', eventType);

    const response = await fetch(`${API_URL}/api/v1/clubs/public?${params.toString()}`, {
        method: 'GET',
        credentials: 'include',
    });

    if (!response.ok) {
        const errorData = await response.json();
        if (!('code' in errorData) || !('message' in errorData)) {
            throw new Error('공개 모임 목록을 가져오는 데 실패했습니다.');
        } else {
            throw new Error(errorData.code + " : " + (errorData.message || '공개 모임 목록을 가져오는 데 실패했습니다.'));
        }
    }

    const data: RsDataPageSimpleClubInfoWithoutLeader = await response.json();
    return data;
}