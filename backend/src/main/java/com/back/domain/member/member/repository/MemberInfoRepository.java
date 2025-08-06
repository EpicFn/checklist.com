package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.MemberInfo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findByEmail(@NotBlank String email);

    Optional<MemberInfo> findByApiKey(String apiKey);

    /**
     * 이메일로 회원 정보 조회 (MemberInfo와 Member를 함께 조회)
     * n+1 방지 fetch join
     */
    @Query("""
            SELECT mi FROM MemberInfo mi
            JOIN FETCH mi.member m
            WHERE mi.email = :email
            """)
    Optional<MemberInfo> findByEmailWithMember(String email);
}
