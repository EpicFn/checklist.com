package com.back.domain.club.club.entity;

import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Club {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.PRIVATE)
  @EqualsAndHashCode.Include
  private Long id;

  @Description("클럽 이름")
  @Column(length = 50, nullable = false)
  private String name;

  @Description("클럽 소개 글")
  @Column(columnDefinition = "TEXT")
  private String bio;

  @Description("클럽 카테고리")
  @Column(length = 50, nullable = false)
  @Enumerated(EnumType.STRING)
  private ClubCategory category;

  @Description("주 모임 장소")
  @Column(length = 256, nullable = false)
  private String mainSpot; // TODO : 지도 연동하면 좌표로 바꿔야 됨

  @Description("최대 인원")
  @Column(nullable = false)
  private int maximumCapacity;

  @Description("인원 모집 여부")
  @Column(nullable = false)
  @Builder.Default
  private boolean recruitingStatus = true;

  @Description("모집 유형")
  @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
  private EventType eventType;

  @Description("시작 날짜")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDate startDate;

  @Description("종료 날짜")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDate endDate;

  @Description("클럽 이미지 URL")
  @Column(length = 256)
  private String imageUrl;

  @Description("클럽 공개 여부")
  @Column(nullable = false)
  private boolean isPublic;

  @Description("클럽장 아이디")
  private Long leaderId;

  @Description("활성화 상태")
  @Column(nullable = false)
  @Builder.Default
  private boolean state = true;

  @Description("구성원")
  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ClubMember> clubMembers = new ArrayList<>();

  @Description("일정 목록")
  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Schedule> clubSchedules = new ArrayList<>();

  // ---------------- 메서드 ----------------
    /**
     * 클럽 활성화 상태를 변경합니다.
     * @param state 활성화 상태
     */
    public void changeState(boolean state) {
        this.state = state;
    }

    /**
     * 클럽의 모집 상태를 변경합니다.
     * @param recruitingStatus 모집 상태
     */
    public void changeRecruitingStatus(boolean recruitingStatus) {
      this.recruitingStatus = recruitingStatus;
    }

  /**
   * 클럽에 새로운 클럽 멤버를 추가합니다.
   * @param clubMember
   */
  public void addClubMember(ClubMember clubMember) {
        this.clubMembers.add(clubMember);
        clubMember.setClub(this); // 양방향 연관관계 설정
  }

  /**
   * 클럽에 새로운 일정을 추가합니다.
   * @param schedule
   */
  public void addClubSchedule(Schedule schedule) {
      this.clubSchedules.add(schedule);
      schedule.setClub(this); // 양방향 연관관계 설정
  }
  /**
   * 클럽의 이미지 URL을 업데이트합니다.
   * @param imageUrl
   */
  public void updateImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
  }

    /**
     * 클럽 정보를 업데이트합니다.
     * @param name 클럽 이름
     * @param bio 클럽 소개
     * @param category 클럽 카테고리
     * @param mainSpot 주 모임 장소
     * @param maximumCapacity 최대 인원
     * @param recruitingStatus 인원 모집 여부
     * @param eventType 모집 유형
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param isPublic 클럽 공개 여부
     */
    public void updateInfo(
            String name,
            String bio,
            ClubCategory category,
            String mainSpot,
            int maximumCapacity,
            boolean recruitingStatus,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            boolean isPublic
    ){
        this.name = name;
        this.bio = bio;
        this.category = category;
        this.mainSpot = mainSpot;
        this.maximumCapacity = maximumCapacity;
        this.recruitingStatus = recruitingStatus;
        this.eventType = eventType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPublic = isPublic;
    }

    public void removeClubMember(ClubMember clubMember) {
      this.clubMembers.remove(clubMember);
      clubMember.setClub(null); // 양방향 연관관계 해제
    }


}
