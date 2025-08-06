package com.back.domain.member.member.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * MemberInfo 엔티티 클래스
 * 멤버의 상세 정보를 담은 엔티티 (회원 전용)
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // equals와 hashCode 메서드를 오버라이드하여 id 필드만 사용
public class MemberInfo {
    // 필드 정의
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE) // Setter는 private로 설정하여 외부에서 변경할 수 없도록 함
    @EqualsAndHashCode.Include
    private Long id; // MemberInfo의 고유 ID

    @Column(unique = true)
    private String email; // 회원 이메일

    private String bio; // 자기소개

    private String profileImageUrl; // 프로필 이미지 URL

    private String apiKey; // 리프레시 토큰 값


    @OneToOne
    @JoinColumn(name = "member_id", unique = true) // member_id 컬럼이 생성되어 Member와 연결됨
    private Member member;


    //===============================빌더==========================
    @Builder
    public MemberInfo(String email, String bio, String profileImageUrl, Member member, String apiKey) {
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.member = member;
        this.apiKey = apiKey;
    }


    //===========================기타 Getter, Setter=======================
    public void setMember(Member member) {
        this.member = member;
    }

    public void updateImageUrl(String imageUrl) {
        if (imageUrl != null) this.profileImageUrl = imageUrl;
    }

    public void updateBio(String bio) {
        if (bio != null) this.bio = bio;
    }
}
