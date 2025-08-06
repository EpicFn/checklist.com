'use client';

import React from 'react';
import { components } from "@/types/backend/apiV1/schema";
import { EventType, EventTypeKorean } from '@/types/EventType';
import { ClubCategory, ClubCategoryKorean } from '@/types/ClubCategory';

type ClubSearchFilter = {
    name: string;
    mainSpot: string;
    clubCategory: ClubCategory | '';
    eventType: EventType | '';
};

type ClubSearchBarProps = {
    value: ClubSearchFilter;
    onChange: (value: ClubSearchFilter) => void;
    onSubmit?: () => void; // Optional submit handler
};

const ClubSearchBar: React.FC<ClubSearchBarProps> = ({ value, onChange, onSubmit }) => {
    const handleChange = (field: keyof ClubSearchFilter, newValue: string) => {
        onChange({
            ...value,
            [field]: newValue,
        });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (onSubmit) onSubmit();
    };

    return (
        <form
            onSubmit={handleSubmit}
            style={{
                display: 'flex',
                gap: '1rem'
            }}
        >
            <input
                type="text"
                placeholder="모임명"
                value={value.name}
                onChange={e => handleChange('name', e.target.value)}
                style={{
                    padding: '0.6rem 1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    flex: 1,
                    minWidth: 120
                }}
            />
            <input
                type="text"
                placeholder="지역"
                value={value.mainSpot}
                onChange={e => handleChange('mainSpot', e.target.value)}
                style={{
                    padding: '0.6rem 1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    flex: 1,
                    minWidth: 100
                }}
            />
            <select
                value={value.clubCategory}
                onChange={e => handleChange('clubCategory', e.target.value)}
                style={{
                    padding: '0.6rem 1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    background: '#f9fafb',
                    flex: 1,
                    minWidth: 110
                }}
            >
                <option value="">카테고리</option>
                {Object.values(ClubCategory).map(cat => (
                    <option key={cat} value={cat}>
                        {ClubCategoryKorean[cat]}
                    </option>
                ))}
            </select>
            <select
                value={value.eventType}
                onChange={e => handleChange('eventType', e.target.value)}
                style={{
                    padding: '0.6rem 1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    background: '#f9fafb',
                    flex: 1,
                    minWidth: 90
                }}
            >
                <option value="">종류</option>
                {Object.values(EventType).map(type => (
                    <option key={type} value={type}>
                        {EventTypeKorean[type]}
                    </option>
                ))}
            </select>
            <button
                type="submit"
                style={{
                    padding: '0.7rem 1.5rem',
                    background: 'linear-gradient(90deg, #6366f1 0%, #60a5fa 100%)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '0.5rem',
                    fontWeight: 600,
                    fontSize: '1rem',
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                }}
            >
                검색
            </button>
        </form>
    );
};

export default ClubSearchBar;