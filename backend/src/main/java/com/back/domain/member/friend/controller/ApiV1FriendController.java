package com.back.domain.member.friend.controller;

import com.back.domain.member.friend.dto.*;
import com.back.domain.member.friend.service.FriendService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members/me/friends")
@RequiredArgsConstructor
@Tag(name = "ApiV1FriendController", description = "친구 컨트롤러")
public class ApiV1FriendController {
    private final FriendService friendService;

    @GetMapping
    @Operation(summary = "내 친구 목록 조회")
    public RsData<List<FriendDto>> getFriends(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) FriendStatusDto status
    ) {
        List<FriendDto> friendDtoList = friendService.getFriends(user.getId(), status);

        return RsData.of(
                200,
                "친구 목록을 성공적으로 조회하였습니다.",
                friendDtoList
        );
    }

    @PostMapping
    @Operation(summary = "친구 추가")
    public RsData<FriendDto> addFriend(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody FriendAddReqBody reqBody
    ) {
        FriendDto friendDto = friendService.addFriend(user.getId(), reqBody.friend_email());

        return RsData.of(
                201,
                "%s 에게 친구 추가 요청이 성공적으로 처리되었습니다.".formatted(reqBody.friend_email()),
                friendDto
        );
    }

    @PatchMapping("/{friendId}/accept")
    @Operation(summary = "친구 요청 수락")
    public RsData<FriendDto> acceptFriend(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long friendId
    ) {
        FriendDto friendDto = friendService.acceptFriend(user.getId(), friendId);

        return RsData.of(
                200,
                "%s님과 친구가 되었습니다.".formatted(friendDto.friendNickname()),
                friendDto
        );
    }

    @PatchMapping("/{friendId}/reject")
    @Operation(summary = "친구 요청 거절")
    public RsData<FriendDto> rejectFriend(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long friendId
    ) {
        FriendDto friendDto = friendService.rejectFriend(user.getId(), friendId);

        return RsData.of(
                200,
                "%s님의 친구 요청을 거절하였습니다.".formatted(friendDto.friendNickname()),
                friendDto
        );
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "친구 삭제")
    public RsData<FriendMemberDto> deleteFriend(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long friendId
    ) {
        FriendMemberDto friendMemberDto = friendService.deleteFriend(user.getId(), friendId);

        return RsData.of(
                200,
                "%s님이 친구 목록에서 삭제되었습니다.".formatted(friendMemberDto.friendNickname()),
                friendMemberDto
        );
    }
}
