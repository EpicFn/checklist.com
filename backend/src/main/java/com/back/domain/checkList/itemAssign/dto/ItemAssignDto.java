package com.back.domain.checkList.itemAssign.dto;

import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.club.clubMember.entity.ClubMember;

public record ItemAssignDto(
    Long id,
    Long clubMemberId,
    String clubMemberName,
    boolean isChecked
) {
  public ItemAssignDto(ItemAssign itemAssign) {
    this(
        itemAssign.getId(),
        itemAssign.getClubMember().getId(),
        itemAssign.getClubMember().getMember().getNickname(),
        itemAssign.isChecked()
    );

  }
}
