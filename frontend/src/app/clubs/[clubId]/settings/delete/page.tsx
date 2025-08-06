'use client';

import React, { use } from "react";
import { useRouter, useParams } from "next/navigation";
import { deleteClub, getClubInfo } from '@/api/club'; // 실제 삭제 API 호출 함수
import { getMyInfoInClub } from "@/api/myClub";
import { useEffect, useState } from "react";
import LoadingSpinner from "@/components/global/LoadingSpinner";

const DeleteClubPage = () => {
    const router = useRouter();
    const params = useParams();
    const clubId = params?.clubId as string;

    const [isLoading, setIsLoading] = useState(true);



    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);
            try {
                const [data, myInfo] = await Promise.all([
                    getClubInfo(clubId),
                    getMyInfoInClub(clubId)
                ]);

                if (!data?.data) {
                    alert("잘못된 접근입니다.");
                    router.push("/");
                }

                // 호스트 권한 조회
                if (myInfo.role !== 'HOST') {
                    alert("호스트만 모임을 삭제할 수 있습니다.");
                    router.push(`/clubs/${clubId}`);
                }

                setIsLoading(false);
            } catch (error) {
                alert("잘못된 접근입니다.");
                router.push("/");
            }
        };

        fetchData();
    }, [clubId, router]);

    const handleDelete = async () => {
        try {
            // 실제 삭제 API 호출 부분 (예시)
            await deleteClub(clubId);
            alert("모임이 삭제되었습니다.");
            router.push("/");
        } catch (error) {
            alert("삭제 중 오류가 발생했습니다.");
        }
    };

    const handleCancel = () => {
        router.back();
    };

    if (isLoading) {
        return <LoadingSpinner />;
    }

    return (
        <div style={{ maxWidth: 400, margin: "80px auto", textAlign: "center" }}>
            <h2>정말로 이 모임을 삭제하시겠습니까?</h2>
            <p>삭제하면 되돌릴 수 없습니다.</p>
            <div style={{ marginTop: 32, display: "flex", gap: 16, justifyContent: "center" }}>
                <button
                    onClick={handleDelete}
                    style={{
                        background: "#e53e3e",
                        color: "#fff",
                        border: "none",
                        padding: "12px 24px",
                        borderRadius: 4,
                        cursor: "pointer",
                    }}
                >
                    삭제하기
                </button>
                <button
                    onClick={handleCancel}
                    style={{
                        background: "#edf2f7",
                        color: "#2d3748",
                        border: "none",
                        padding: "12px 24px",
                        borderRadius: 4,
                        cursor: "pointer",
                    }}
                >
                    취소
                </button>
            </div>
        </div>
    );
};

export default DeleteClubPage;