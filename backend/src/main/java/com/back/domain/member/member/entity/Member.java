package com.back.domain.member.member.entity;


import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.friend.entity.Friend;
import com.back.global.enums.MemberType;
import com.back.domain.preset.preset.entity.Preset;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.PRIVATE)
  @EqualsAndHashCode.Include
  private Long id;

  @Description("닉네임")
  @Column(length = 50, nullable = false)
  private String nickname;

  @Description("비밀번호")
  private String password;

  @Description("회원, 비회원 여부")
  @Enumerated(EnumType.STRING)
  private MemberType memberType;

  @Description("중복 닉네임 구분용")
  private String tag;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "member")
  private MemberInfo memberInfo; // 상세 정보 (회원 전용)

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "owner")
  private List<Preset> presets; // 프리셋 목록 (회원 전용)

  // 친구 관계 (내가 포함된 모든 관계)
  @OneToMany(mappedBy = "member1", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Friend> friendshipsAsMember1;
  @OneToMany(mappedBy = "member2", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Friend> friendshipsAsMember2;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClubMember> clubMembers; // 소속 그룹 목록


  //==========================빌더, 빌더 메소드==========================
  @Builder
  public Member(String nickname, String password, MemberType memberType, String tag, MemberInfo memberInfo) {
    this.nickname = nickname;
    this.password = password;
    this.memberType = memberType;
    this.tag = tag;
    this.memberInfo = memberInfo;
  }

  public static Member createGuest(String nickname, String password, String tag) {
    return Member.builder()
            .nickname(nickname)
            .password(password)
            .tag(tag)
            .memberType(MemberType.GUEST)
            .build();
  }

  public static Member createMember(String nickname, String password, String tag) {
    return Member.builder()
            .nickname(nickname)
            .password(password)
            .tag(tag)
            .memberType(MemberType.MEMBER)
            .build();
  }


//===========================기타 Getter, Setter=======================
  public void setMemberInfo(MemberInfo memberInfo) {
    this.memberInfo = memberInfo;
    if (memberInfo != null && memberInfo.getMember() != this) {
      memberInfo.setMember(this);
    }
  }

  public void updateInfo(String nickname, String tag, String password) {
    if (nickname != null) this.nickname = nickname;
    if (tag != null) this.tag = tag;
    if (password != null) this.password = password;
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public String getEmail() {
    return memberInfo != null ? memberInfo.getEmail() : null;
  }
}
