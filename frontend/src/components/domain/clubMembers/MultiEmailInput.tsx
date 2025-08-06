'use client';

import { useState, KeyboardEvent } from 'react';

// 부모 컴포넌트에게 전달할 props 타입 정의
interface MultiEmailInputProps {
    initialEmails?: string[];
    onSubmit: (emails: string[]) => void;
    placeholder?: string;
    label?: string;
}

export default function MultiEmailInput({
    initialEmails = [],
    onSubmit,
    placeholder = "이메일을 입력하고 Enter를 누르세요...",
    label = "초대할 멤버 이메일"
}: MultiEmailInputProps) {
    // 1. 상태 관리
    const [inputValue, setInputValue] = useState(''); // 현재 입력창의 값
    const [emails, setEmails] = useState<string[]>(initialEmails); // 추가된 이메일 목록
    const [error, setError] = useState<string | null>(null); // 유효성 검사 에러 메시지

    // 2. 이메일 유효성 검사 함수
    const isValidEmail = (email: string) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    };

    // 3. Enter 키 입력 처리
    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault(); // form의 기본 제출 동작 방지

            const newEmail = inputValue.trim();

            if (newEmail) {
                if (!isValidEmail(newEmail)) {
                    setError('유효한 이메일 형식이 아닙니다.');
                    return;
                }
                if (emails.includes(newEmail)) {
                    setError('이미 추가된 이메일입니다.');
                    return;
                }

                setEmails([...emails, newEmail]);
                setInputValue('');
                setError(null);
            }
        }
    };

    // 4. 이메일 태그 삭제 처리
    const handleDeleteEmail = (emailToDelete: string) => {
        setEmails(emails.filter(email => email !== emailToDelete));
    };

    // 5. 최종 제출 처리
    const handleSubmit = () => {
        // 입력창에 남아있는 텍스트가 유효한 이메일이면 목록에 추가 후 제출
        const lastEmail = inputValue.trim();
        let finalEmails = [...emails];

        if (lastEmail) {
            if (!isValidEmail(lastEmail) || emails.includes(lastEmail)) {
                setError('입력창의 이메일이 유효하지 않거나 중복되었습니다. 확인 후 다시 시도해주세요.');
                return;
            }
            finalEmails.push(lastEmail);
        }

        onSubmit(finalEmails);
        // 제출 후 초기화
        setEmails([]);
        setInputValue('');
    };

    return (
        <div className="w-full">
            <label htmlFor="email-input" className="block text-sm font-medium text-gray-700 mb-1">
                {label}
            </label>
            <div className="flex flex-wrap items-center gap-2 p-2 border border-gray-300 rounded-md focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-blue-500">
                {emails.map((email) => (
                    <div key={email} className="bg-blue-100 text-blue-800 text-sm font-medium px-3 py-1 rounded-full flex items-center gap-2" onClick={() => handleDeleteEmail(email)}>
                        <span>{email}</span>
                    </div>
                ))}
                <input
                    id="email-input"
                    type="email"
                    value={inputValue}
                    onChange={(e) => {
                        setInputValue(e.target.value);
                        if (error) setError(null); // 입력 시 에러 메시지 초기화
                    }}
                    onKeyDown={handleKeyDown}
                    className="flex-grow p-1 outline-none bg-transparent text-sm"
                    placeholder={emails.length === 0 ? placeholder : ''}
                />
            </div>
            {error && <p className="text-red-500 text-xs mt-1">{error}</p>}

            <div className="mt-4 text-right">
                <button
                    onClick={handleSubmit}
                    className="px-6 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                    제출하기
                </button>
            </div>
        </div>
    );
}