package com.back.domain.schedule.schedule.entity;

import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.club.club.entity.Club;
import com.back.domain.member.member.entity.Member;
import com.back.domain.preset.preset.entity.PresetItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("일정 제목")
    private String title;

    @Description("일정 내용")
    private String content;

    @Description("일정 시작 날짜")
    private LocalDateTime startDate;

    @Description("일정 종료 날짜")
    private LocalDateTime endDate;

    @Description("일정 장소")
    private String spot; //TODO : 나중에 지도 연동하면 좌표로 변경

    @Description("활성화 여부")
    private boolean isActive = true;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Club club; // 그룹 일정

    @Setter
    @OneToOne(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private CheckList checkList;

    @Builder
    public Schedule(String title, String content, LocalDateTime startDate, LocalDateTime endDate, String spot, Club club) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.spot = spot;
        if (club != null) {
            this.club = club;
            club.addClubSchedule(this);
        }
    }


    public void updateCheckList(CheckList checkList) {
        this.checkList = checkList;
    }

    // 일정 수정
    public void modify(String title, String content, LocalDateTime startDate, LocalDateTime endDate, String spot) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.spot = spot;
    }

    // 일정 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // 일정 db 삭제 가능 여부
    public boolean canDelete() {
        return checkList == null || !checkList.isActive();
    }
}
