package com.back.domain.club.clubMember.entity;

import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.club.club.entity.Club;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("멤버 정보")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Description("역할")
    @Enumerated(EnumType.STRING)
    private ClubMemberRole role;

    @Description("가입 상태")
    @Enumerated(EnumType.STRING)
    private ClubMemberState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    @Setter
    private Club club;

    @Description("체크리스트 아이템 할당 정보")
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemAssign> itemAssigns;

    public void addItemAssign(ItemAssign itemAssign) {
        this.itemAssigns.add(itemAssign);
        itemAssign.setClubMember(this);
    }

    public void updateState(ClubMemberState newState) {
        this.state = newState;
    }

    public void updateRole(ClubMemberRole newRole) { this.role = newRole; }
}
