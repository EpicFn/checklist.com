package com.back.domain.club.clubLink.entity;

import com.back.domain.club.club.entity.Club;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClubLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("초대 코드")
    @Column(unique = true, nullable = false, length = 50)
    private String inviteCode;

    @Description("링크 생성 날짜")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Description("링크 만료 날짜")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime expiresAt;

    @Description("클럽 정보")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;


    //===================================빌더=========================================
    @Builder
    public ClubLink(String inviteCode, LocalDateTime createdAt, LocalDateTime expiresAt, Club club) {
        this.inviteCode = inviteCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.club = club;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
