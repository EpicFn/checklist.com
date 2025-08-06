package com.back.domain.checkList.itemAssign.entity;

import com.back.domain.checkList.checkList.entity.CheckListItem;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;
import com.back.domain.club.clubMember.entity.ClubMember;
/**
 * 멤버를 checklistItem에 할당하는 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_member_id", "check_list_item_id"})
)
public class ItemAssign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_member_id", nullable = false)
    @Description("할당된 인원")
    @Setter
    private ClubMember clubMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_list_item_id", nullable = false)
    @Description("할당된 체크리스트 아이템")
    @Setter
    private CheckListItem checkListItem;

    @Description("체크 여부")
    private boolean isChecked;
}
