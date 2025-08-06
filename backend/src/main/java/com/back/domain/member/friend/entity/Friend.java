package com.back.domain.member.friend.entity;

import com.back.domain.member.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    // id 가 낮은 멤버가 member1, 높은 멤버가 member2
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member2;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member requestedBy; // 요청자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private FriendStatus status;

    @CreatedDate
    private LocalDateTime createdDate;

    // ===== 유틸 메서드 =====

    /**
     * 친구 관계가 member1, member2 중 하나라도 포함되어 있는지 확인합니다.
     * @param member 확인할 멤버
     * @return 포함되어 있으면 true, 아니면 false
     */
    public boolean involves(Member member) {
        return member1.equals(member) || member2.equals(member);
    }

    /**
     * 이 친구 관계에 포함된 다른 멤버를 반환합니다.
     * @param self 자신을 나타내는 멤버
     * @return 다른 멤버
     * @throws IllegalArgumentException 만약 self가 이 친구 관계에 포함되지 않는 경우
     */
    public Member getOther(Member self) {
        if (member1.equals(self)) return member2;
        if (member2.equals(self)) return member1;
        throw new IllegalArgumentException("해당 멤버는 이 친구 관계에 포함되지 않습니다.");
    }
}
