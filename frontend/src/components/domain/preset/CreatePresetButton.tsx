'use client';

interface CreatePresetButtonProps {
  onClick: () => void;
}

export default function CreatePresetButton({ onClick }: CreatePresetButtonProps) {
  return (
    <button
      onClick={onClick}
      className="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-semibold py-3 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 flex items-center space-x-2"
    >
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
      </svg>
      <span>새 프리셋 만들기</span>
    </button>
  );
} 