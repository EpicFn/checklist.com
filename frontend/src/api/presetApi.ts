import { PresetListResponse, ApiErrorResponse, Preset, PresetWriteReqDto, RsDataPresetDto } from '@/types/preset';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export async function fetchPresets(): Promise<PresetListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/presets`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return data;
  } catch (error) {
    throw error;
  }
}

export async function fetchPresetDetail(presetId: string): Promise<Preset> {
  try {
    const response = await fetch(`${API_BASE_URL}/presets/${presetId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const result = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = result as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return result.data;
  } catch (error) {
    throw error;
  }
}

export async function createPreset(preset: PresetWriteReqDto): Promise<Preset> {
  try {
    const response = await fetch(`${API_BASE_URL}/presets`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(preset),
    });

    const result = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = result as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return result.data;
  } catch (error) {
    throw error;
  }
}

export async function updatePreset(presetId: string, preset: Preset): Promise<Preset> {
  try {
    // Preset을 DTO 구조로 변환
    const updateDto = {
      name: preset.name,
      presetItems: preset.presetItems?.map((item, index) => ({
        content: item.content,
        category: item.category,
        sequence: item.sequence || index + 1,
      })) || []
    };


    const response = await fetch(`${API_BASE_URL}/presets/${presetId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(updateDto),
    });

    const result = await response.json();

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const errorData = result as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return result.data;
  } catch (error) {
    throw error;
  }
}

export async function deletePreset(presetId: string): Promise<void> {
  try {
    const response = await fetch(`${API_BASE_URL}/presets/${presetId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    // 401 에러인 경우 (로그인 필요)
    if (response.status === 401) {
      const data = await response.json();
      const errorData = data as ApiErrorResponse;
      throw new Error(`LOGIN_REQUIRED:${errorData.message}`);
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    throw error;
  }
}