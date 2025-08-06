package com.back.domain.checkList.checkList.entity;

import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.global.enums.CheckListItemCategory;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CheckListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("체크리스트 아이템 내용")
    private String content;

    @Description("체크리스트 아이템 카테고리")
    @Enumerated(EnumType.STRING)
    private CheckListItemCategory category;

    @Description("정렬 순서")
    private int sequence;

    @Description("체크리스트 이름")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_list_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private CheckList checkList;

    @Description("체크 아이템 완료 여부")
    private boolean isChecked; // 기본값 false, false인 경우 세부 상황까지 체크하여 최종 결정

    @Description("할당된 인원")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "checkListItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemAssign> itemAssigns;

    @Builder
    public CheckListItem(String content, CheckListItemCategory category, int sequence, CheckList checkList, boolean isChecked, List<ItemAssign> itemAssigns) {
        this.content = content;
        this.category = category;
        this.sequence = sequence;
        this.checkList = checkList;
        this.isChecked = isChecked;
        if (itemAssigns != null) {
            this.itemAssigns = itemAssigns;
            // 양방향 연관관계 설정
            itemAssigns.forEach(item -> item.setCheckListItem(this));
        }
    }
}