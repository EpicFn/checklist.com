package com.back.domain.preset.preset.entity;

import com.back.domain.member.member.entity.Member;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Preset {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Description("프리셋 이름")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member owner;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "preset")
    private List<PresetItem> presetItems;

    @Builder
    public Preset(String name, Member owner, List<PresetItem> presetItems) {
        this.name = name;
        this.owner = owner;
        if (presetItems != null) {
            this.presetItems = presetItems;
            // 양방향 연관관계 설정
            presetItems.forEach(item -> item.setPreset(this));
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePresetItems(List<PresetItem> presetItems) {
        this.presetItems.clear();
        this.presetItems.addAll(presetItems);
    }

}
