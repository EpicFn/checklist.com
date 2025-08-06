package com.back.domain.preset.preset.entity;

import com.back.global.enums.CheckListItemCategory;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PresetItem {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Description("프리셋 아이템의 내용")
    private String content; // 내용

    @Description("프리셋 아이템의 카테고리")
    @Enumerated(EnumType.STRING)
    private CheckListItemCategory category; // 카테고리 (enum 변경 예정))

    @Description("프리셋 아이템의 정렬 순서")
    private int sequence; // 정렬 순서

    @Setter
    @Description("아이템이 속한 프리셋")
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    private Preset preset; // 프리셋

}
