import React from "react";
import { ArrowLeft, ArrowRight } from "@/components/global/arrows";

interface PagingUnitProps {
    page: number;
    totalPages: number;
    setPage: (page: number) => void;
    className?: string;
}

const PagingUnit: React.FC<PagingUnitProps> = ({
    page,
    totalPages,
    setPage,
    className = "",
}) => {
    const handlePrev = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleNext = () => {
        if (page + 1 < totalPages) setPage(page + 1);
    };

    return (
        <div className={`flex justify-center items-center gap-4 mt-6 ${className}`}>
            <button
                disabled={page === 0}
                onClick={handlePrev}
                className="px-3 py-2 rounded-full bg-white border border-gray-300 shadow-sm hover:bg-gray-100 transition disabled:opacity-40 disabled:cursor-not-allowed"
                aria-label="이전 페이지"
            >
                <ArrowLeft />
            </button>
            <span className="px-4 py-2 rounded bg-gray-100 text-gray-700 font-semibold shadow">
                {page + 1} <span className="text-gray-400">/</span> {totalPages}
            </span>
            <button
                disabled={page + 1 >= totalPages}
                onClick={handleNext}
                className="px-3 py-2 rounded-full bg-white border border-gray-300 shadow-sm hover:bg-gray-100 transition disabled:opacity-40 disabled:cursor-not-allowed"
                aria-label="다음 페이지"
            >
                <ArrowRight />
            </button>
        </div>
    );
};

export default PagingUnit;