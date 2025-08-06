'use client';

import { useState, useRef, useEffect } from 'react';
import { EventType, EventTypeKorean } from '@/types/EventType';
import { ClubCategory, ClubCategoryKorean } from '@/types/ClubCategory';


export interface ClubFormData {
    name: string;
    bio: string;
    mainSpot: string;
    maximumCapacity: number;
    category: ClubCategory;
    eventType: EventType;
    activityPeriod: {
        startDate: string;
        endDate: string;
    };
    isPublic: boolean;
}

interface ClubDataFormProps {
    onSubmit: (data: ClubFormData, image: File | null) => void;
    isLoading?: boolean;
    initialData?: Partial<ClubFormData>;
    initialImage?: File | null;
    initialImageUrl?: string | null;
}


export default function ClubDataForm({ onSubmit, isLoading = false, initialData, initialImage, initialImageUrl }: ClubDataFormProps) {
    const [formData, setFormData] = useState<ClubFormData>({
        name: initialData?.name || '',
        bio: initialData?.bio || '',
        mainSpot: initialData?.mainSpot || '',
        maximumCapacity: initialData?.maximumCapacity || 10,
        category: initialData?.category || ClubCategory.OTHER,
        eventType: initialData?.eventType || EventType.ONE_TIME,
        activityPeriod: initialData?.activityPeriod || {
            startDate: '',
            endDate: ''
        },
        isPublic: initialData?.isPublic ?? true
    });

    const [image, setImage] = useState<File | null>(initialImage || null);
    const [errors, setErrors] = useState<Partial<Record<keyof ClubFormData, string>>>({});
    const [imageError, setImageError] = useState<string>('');
    const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(
        initialImageUrl || null
    );
    const fileInputRef = useRef<HTMLInputElement>(null);

    // 이미지 변경 시 URL 정리
    useEffect(() => {
        return () => {
            if (imagePreviewUrl) {
                URL.revokeObjectURL(imagePreviewUrl);
            }
        };
    }, [imagePreviewUrl]);

    const handleInputChange = (field: keyof ClubFormData, value: any) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        // 에러 메시지 초기화
        if (errors[field]) {
            setErrors(prev => ({
                ...prev,
                [field]: ''
            }));
        }
    };

    const handleActivityPeriodChange = (field: 'startDate' | 'endDate', value: string) => {
        setFormData(prev => ({
            ...prev,
            activityPeriod: {
                ...prev.activityPeriod,
                [field]: value
            }
        }));
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            // 파일 크기 검증 (5MB 제한)
            if (file.size > 5 * 1024 * 1024) {
                setImageError('이미지 파일 크기는 5MB 이하여야 합니다.');
                return;
            }

            // 파일 타입 검증
            if (!file.type.startsWith('image/')) {
                setImageError('이미지 파일만 업로드 가능합니다.');
                return;
            }

            setImage(file);

            if (imagePreviewUrl)
                URL.revokeObjectURL(imagePreviewUrl);
            setImagePreviewUrl(URL.createObjectURL(file));

            setImageError('');
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Partial<Record<keyof ClubFormData, string>> = {};

        if (!formData.name.trim()) {
            newErrors.name = '모임명을 입력해주세요.';
        }

        if (!formData.bio.trim()) {
            newErrors.bio = '모임 소개글을 입력해주세요.';
        }

        if (!formData.mainSpot.trim()) {
            newErrors.mainSpot = '주 모임 장소를 입력해주세요.';
        }

        if (formData.maximumCapacity < 2) {
            newErrors.maximumCapacity = '가입 정원은 최소 2명 이상이어야 합니다.';
        }

        if (!formData.category) {
            newErrors.category = '카테고리를 선택해주세요.';
        }

        if (!formData.eventType) {
            newErrors.eventType = '이벤트 타입을 선택해주세요.';
        }

        if (!formData.activityPeriod.startDate) {
            newErrors.activityPeriod = '활동 시작일을 선택해주세요.';
        }

        if (!formData.activityPeriod.endDate) {
            newErrors.activityPeriod = '활동 종료일을 선택해주세요.';
        }

        if (formData.activityPeriod.startDate && formData.activityPeriod.endDate) {
            const startDate = new Date(formData.activityPeriod.startDate);
            const endDate = new Date(formData.activityPeriod.endDate);

            if (startDate >= endDate) {
                newErrors.activityPeriod = '활동 종료일은 시작일보다 늦어야 합니다.';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0 && !imageError;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (validateForm()) {
            let imageToSend: File | null | undefined;

            if (image) {
                // 새 이미지 선택됨 → 전송
                imageToSend = image;
            } else if (initialImageUrl) {
                // 기존 이미지 유지 → null로 전송해서 서버에서 유지
                imageToSend = null;
            } else {
                // 이미지 없음 → null로 전송
                imageToSend = null;
            }

            onSubmit(formData, imageToSend);
        }
    };

    const handleImageClick = () => {
        fileInputRef.current?.click();
    };

    return (
        <form onSubmit={handleSubmit} className="max-w-2xl mx-auto p-6 space-y-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">모임 정보 입력</h2>

            {/* 모임명 */}
            <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                    모임명 *
                </label>
                <input
                    type="text"
                    id="name"
                    value={formData.name}
                    onChange={(e) => handleInputChange('name', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.name ? 'border-red-500' : 'border-gray-300'
                        }`}
                    placeholder="모임명을 입력하세요"
                />
                {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name}</p>}
            </div>

            {/* 대표 이미지 */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    대표 이미지
                </label>
                <div
                    onClick={handleImageClick}
                    className="w-full h-48 border-2 border-dashed border-gray-300 rounded-md flex flex-col items-center justify-center cursor-pointer hover:border-gray-400 transition-colors"
                >
                    {imagePreviewUrl ? (
                        <div className="relative w-full h-full">
                            <img
                                src={imagePreviewUrl}
                                alt="대표 이미지"
                                className="w-full h-full object-cover rounded-md"
                            />
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setImage(null);
                                    setImagePreviewUrl(null);
                                    setImageError('');
                                }}
                                className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600"
                            >
                                ×
                            </button>
                        </div>
                    ) : (
                        <div className="text-center">
                            <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
                            </svg>
                            <p className="mt-2 text-sm text-gray-600">이미지를 클릭하여 업로드하세요</p>
                            <p className="text-xs text-gray-500">PNG, JPG, GIF (최대 5MB)</p>
                        </div>
                    )}
                </div>
                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="hidden"
                />
                {imageError && <p className="mt-1 text-sm text-red-600">{imageError}</p>}
            </div>

            {/* 모임 소개글 */}
            <div>
                <label htmlFor="bio" className="block text-sm font-medium text-gray-700 mb-2">
                    모임 소개글 *
                </label>
                <textarea
                    id="bio"
                    value={formData.bio}
                    onChange={(e) => handleInputChange('bio', e.target.value)}
                    rows={4}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.bio ? 'border-red-500' : 'border-gray-300'
                        }`}
                    placeholder="모임에 대한 소개글을 작성해주세요"
                />
                {errors.bio && <p className="mt-1 text-sm text-red-600">{errors.bio}</p>}
            </div>

            {/* 주 모임 장소 */}
            <div>
                <label htmlFor="mainSpot" className="block text-sm font-medium text-gray-700 mb-2">
                    주 모임 장소 *
                </label>
                <input
                    type="text"
                    id="mainSpot"
                    value={formData.mainSpot}
                    onChange={(e) => handleInputChange('mainSpot', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.mainSpot ? 'border-red-500' : 'border-gray-300'
                        }`}
                    placeholder="주 모임 장소를 입력하세요"
                />
                {errors.mainSpot && <p className="mt-1 text-sm text-red-600">{errors.mainSpot}</p>}
            </div>

            {/* 가입 정원 */}
            <div>
                <label htmlFor="maximumCapacity" className="block text-sm font-medium text-gray-700 mb-2">
                    가입 정원 *
                </label>
                <input
                    type="number"
                    id="maximumCapacity"
                    min="2"
                    max="100"
                    value={formData.maximumCapacity}
                    onChange={(e) => handleInputChange('maximumCapacity', parseInt(e.target.value) || 2)}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.maximumCapacity ? 'border-red-500' : 'border-gray-300'
                        }`}
                />
                {errors.maximumCapacity && <p className="mt-1 text-sm text-red-600">{errors.maximumCapacity}</p>}
            </div>

            {/* 카테고리 */}
            <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
                    카테고리 *
                </label>
                <select
                    id="category"
                    value={formData.category}
                    onChange={(e) => handleInputChange('category', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.category ? 'border-red-500' : 'border-gray-300'
                        }`}
                >
                    {Object.values(ClubCategory).map((category) => (
                        <option key={category} value={category}>
                            {ClubCategoryKorean[category] || category}
                        </option>
                    ))}
                </select>
                {errors.category && <p className="mt-1 text-sm text-red-600">{errors.category}</p>}
            </div>

            {/* 이벤트 타입 */}
            <div>
                <label htmlFor="eventType" className="block text-sm font-medium text-gray-700 mb-2">
                    이벤트 타입 *
                </label>
                <select
                    id="eventType"
                    value={formData.eventType}
                    onChange={(e) => handleInputChange('eventType', e.target.value as "ONE_TIME" | "SHORT_TERM" | "LONG_TERM")}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.eventType ? 'border-red-500' : 'border-gray-300'
                        }`}
                >
                    {Object.values(EventType).map((eventType) => (
                        <option key={eventType} value={eventType}>
                            {EventTypeKorean[eventType] || eventType}
                        </option>
                    ))}
                </select>
                {errors.eventType && <p className="mt-1 text-sm text-red-600">{errors.eventType}</p>}
            </div>

            {/* 활동기간 */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    활동기간 *
                </label>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="startDate" className="block text-xs text-gray-600 mb-1">
                            시작일
                        </label>
                        <input
                            type="date"
                            id="startDate"
                            value={formData.activityPeriod.startDate}
                            onChange={(e) => handleActivityPeriodChange('startDate', e.target.value)}
                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.activityPeriod ? 'border-red-500' : 'border-gray-300'
                                }`}
                        />
                    </div>
                    <div>
                        <label htmlFor="endDate" className="block text-xs text-gray-600 mb-1">
                            종료일
                        </label>
                        <input
                            type="date"
                            id="endDate"
                            value={formData.activityPeriod.endDate}
                            onChange={(e) => handleActivityPeriodChange('endDate', e.target.value)}
                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.activityPeriod ? 'border-red-500' : 'border-gray-300'
                                }`}
                        />
                    </div>
                </div>
                {errors.activityPeriod && <p className="mt-1 text-sm text-red-600">{errors.activityPeriod}</p>}
            </div>

            {/* 공개/비공개 여부 */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    공개 설정
                </label>
                <div className="flex items-center space-x-4">
                    <label className="flex items-center">
                        <input
                            type="radio"
                            name="isPublic"
                            checked={formData.isPublic}
                            onChange={() => handleInputChange('isPublic', true)}
                            className="mr-2"
                        />
                        <span className="text-sm text-gray-700">공개</span>
                    </label>
                    <label className="flex items-center">
                        <input
                            type="radio"
                            name="isPublic"
                            checked={!formData.isPublic}
                            onChange={() => handleInputChange('isPublic', false)}
                            className="mr-2"
                        />
                        <span className="text-sm text-gray-700">비공개</span>
                    </label>
                </div>
                <p className="mt-1 text-xs text-gray-500">
                    {formData.isPublic
                        ? '공개 모임은 모든 사용자가 검색하고 가입할 수 있습니다.'
                        : '비공개 모임은 초대를 통해서만 가입할 수 있습니다.'
                    }
                </p>
            </div>

            {/* 제출 버튼 */}
            <div className="pt-4">
                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                >
                    {isLoading ? '처리 중...' : '확인'}
                </button>
            </div>
        </form>
    );
}
