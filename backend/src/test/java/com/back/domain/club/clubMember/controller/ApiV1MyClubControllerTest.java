package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MyClubControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClubService clubService;
    @Autowired
    private ClubMemberService clubMemberService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ClubMemberRepository clubMemberRepository;


    @MockitoBean
    private S3Service s3Service; // S3Service는 MockBean으로 주입하여 실제 S3와의 통신을 피합니다
    @Test
    @DisplayName("모임 초대 수락")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );

        // when
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/my-clubs/" + club.getId() + "/join")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대를 수락했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증: 클럽 멤버 목록에 초대된 멤버가 포함되어 있는지 확인
        assertThat(club.getClubMembers().get(1).getMember().getId()).isEqualTo(invitedMember.getId());
        assertThat(club.getClubMembers().get(1).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
        assertThat(club.getClubMembers().get(1).getState()).isEqualTo(ClubMemberState.JOINING);
    }

    @Test
    @DisplayName("모임 초대 거절")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void rejectClubInvitation() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );


        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/my-clubs/" + club.getId() + "/invitation")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("rejectClubInvitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대를 거절했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증:
        assertThat(club.getClubMembers().size()).isEqualTo(1); // 초대된 멤버가 거절했으므로 클럽 멤버 수는 1명이어야 함
        assertThat(club.getClubMembers().get(0).getMember().getId()).isEqualTo(hostMember.getId());
    }

    @Test
    @DisplayName("모임 초대 수락 - 초대 상태가 아닌 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_NotInvited() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );


        // when
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/my-clubs/" + club.getId() + "/join")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("클럽 초대 상태가 아닙니다."));
    }

    @Test
    @DisplayName("모임 초대 수락 - 이미 가입 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_AlreadyJoined() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 이미 가입된 멤버 추가 (1번을 이미 가입 상태로 추가)
        Member alreadyJoinedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("이미 가입된 멤버가 존재하지 않습니다."));

        ClubMember alreadyClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                alreadyJoinedMember,
                ClubMemberRole.PARTICIPANT
        );

        alreadyClubMember.updateState(ClubMemberState.JOINING); // 이미 가입 상태로 업데이트

        // when
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/my-clubs/" + club.getId() + "/join")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 가입 상태입니다."));
    }

    @Test
    @DisplayName("모임 초대 수락 - 이미 가입 신청 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_AlreadyApplying() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 가입 신청 중인 멤버 추가 (1번을 가입 신청 상태로 추가)
        Member applyingMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("가입 신청 중인 멤버가 존재하지 않습니다."));

        ClubMember applyingClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                applyingMember,
                ClubMemberRole.PARTICIPANT
        );

        applyingClubMember.updateState(ClubMemberState.APPLYING); // 가입 신청 상태로 업데이트
        clubMemberRepository.save(applyingClubMember);


        // when
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/my-clubs/" + club.getId() + "/join")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value( "클럽 초대 상태가 아닙니다."));
    }

    // 잘못된 클럽
    @Test
    @DisplayName("잘못된 클럽 ID로 모임 초대 수락 시도")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_InvalidClubId() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/my-clubs/999/join") // 존재하지 않는 클럽 ID
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("공개 모임 가입 신청")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 가입 신청을 완료했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증: 클럽 멤버 목록에 신청한 멤버가 포함되어 있는지 확인
        assertThat(club.getClubMembers().get(1).getMember().getId()).isEqualTo(1L);
        assertThat(club.getClubMembers().get(1).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
        assertThat(club.getClubMembers().get(1).getState()).isEqualTo(ClubMemberState.APPLYING);
    }

    @Test
    @DisplayName("공개 모임 가입 신청 - 이미 가입 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub_AlreadyJoined() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 이미 가입된 멤버 추가 (1번을 이미 가입 상태로 추가)
        Member alreadyJoinedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("이미 가입된 멤버가 존재하지 않습니다."));

        ClubMember alreadyClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                alreadyJoinedMember,
                ClubMemberRole.PARTICIPANT
        );

        alreadyClubMember.updateState(ClubMemberState.JOINING); // 이미 가입 상태로 업데이트

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 가입 상태입니다."));
    }

    @Test
    @DisplayName("공개 모임 가입 신청 - 이미 가입 신청 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub_AlreadyApplying() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 가입 신청 중인 멤버 추가 (1번을 가입 신청 상태로 추가)
        Member applyingMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("가입 신청 중인 멤버가 존재하지 않습니다."));

        ClubMember applyingClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                applyingMember,
                ClubMemberRole.PARTICIPANT
        );

        applyingClubMember.updateState(ClubMemberState.APPLYING); // 가입 신청 상태로 업데이트

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 가입 신청 상태입니다."));
    }

    @Test
    @DisplayName("공개 모임 가입 신청 - 초대된 상태일때 에러")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub_InvitedState() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 초대된 멤버 추가 (1번을 초대 상태로 추가)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        ClubMember invitedClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );

        invitedClubMember.updateState(ClubMemberState.INVITED); // 초대 상태로 업데이트

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("클럽 초대 상태입니다. 초대를 수락해주세요."));
    }

    @Test
    @DisplayName("잘못된 클럽 ID로 공개 모임 가입 신청 시도")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub_InvalidClubId() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/999/apply") // 존재하지 않는 클럽 ID
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("공개 모임 가입 신청 - 클럽이 비공개인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void applyForPublicClub_PrivateClub() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("비공개 그룹")
                        .bio("비공개 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(false) // 비공개 클럽
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("applyForPublicClub"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("비공개 클럽입니다. 가입 신청이 불가능합니다."));
    }

    @Test
    @DisplayName("클럽에서의 내 정보 반환")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void getMyClubInfo() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/my-clubs/" + club.getId())
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("getMyClubInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 멤버 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()))
                .andExpect(jsonPath("$.data.role").value("PARTICIPANT"))
                .andExpect(jsonPath("$.data.state").value("INVITED"));
    }

    @Test
    @DisplayName("클럽에서의 내 정보 반환 - 클럽이 존재하지 않는 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void getMyClubInfo_InvalidClubId() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/my-clubs/999") // 존재하지 않는 클럽 ID
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("getMyClubInfo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("클럽에서의 내 정보 반환 - 클럽에 가입하지 않은 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void getMyClubInfo_NotJoined() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/my-clubs/" + club.getId())
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("getMyClubInfo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽 멤버 정보가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("내 클럽 목록 반환")
    @WithUserDetails(value = "uny@test.com") // 6번 멤버로 로그인
    public void getMyClubs() throws Exception {
        // given
        Club club1 = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹 1")
                        .bio("테스트 그룹 1 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        Club club2 = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹 2")
                        .bio("테스트 그룹 2 설명")
                        .category(ClubCategory.SPORTS)
                        .mainSpot("부산")
                        .maximumCapacity(15)
                        .eventType(EventType.LONG_TERM)
                        .startDate(LocalDate.of(2023, 11, 1))
                        .endDate(LocalDate.of(2023, 12, 31))
                        .isPublic(false)
                        .leaderId(3L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember1 = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club1.getId(),
                hostMember1,
                ClubMemberRole.HOST
        );

        Member hostMember2 = memberService.findMemberById(3L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club2.getId(),
                hostMember2,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(6L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club1.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );
        clubMemberService.addMemberToClub(
                club2.getId(),
                invitedMember,
                ClubMemberRole.MANAGER
        );

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/my-clubs")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("getMyClubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("내 클럽 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.clubs.length()").value(2)) // 2개의 클럽이 있어야 함

                .andExpect(jsonPath("$.data.clubs[0].clubId").value(club1.getId()))
                .andExpect(jsonPath("$.data.clubs[0].clubName").value(club1.getName()))
                .andExpect(jsonPath("$.data.clubs[0].bio").value(club1.getBio()))
                .andExpect(jsonPath("$.data.clubs[0].category").value(club1.getCategory().name()))
                .andExpect(jsonPath("$.data.clubs[0].imageUrl").value(club1.getImageUrl()))
                .andExpect(jsonPath("$.data.clubs[0].mainSpot").value(club1.getMainSpot()))
                .andExpect(jsonPath("$.data.clubs[0].eventType").value(club1.getEventType().name()))
                .andExpect(jsonPath("$.data.clubs[0].startDate").value(club1.getStartDate().toString()))
                .andExpect(jsonPath("$.data.clubs[0].endDate").value(club1.getEndDate().toString()))
                .andExpect(jsonPath("$.data.clubs[0].isPublic").value(club1.isPublic()))
                .andExpect(jsonPath("$.data.clubs[0].myRole").value("PARTICIPANT"))
                .andExpect(jsonPath("$.data.clubs[0].myState").value("INVITED"))

                .andExpect(jsonPath("$.data.clubs[1].clubId").value(club2.getId()))
                .andExpect(jsonPath("$.data.clubs[1].clubName").value(club2.getName()))
                .andExpect(jsonPath("$.data.clubs[1].bio").value(club2.getBio()))
                .andExpect(jsonPath("$.data.clubs[1].category").value(club2.getCategory().name()))
                .andExpect(jsonPath("$.data.clubs[1].imageUrl").value(club2.getImageUrl()))
                .andExpect(jsonPath("$.data.clubs[1].mainSpot").value(club2.getMainSpot()))
                .andExpect(jsonPath("$.data.clubs[1].eventType").value(club2.getEventType().name()))
                .andExpect(jsonPath("$.data.clubs[1].startDate").value(club2.getStartDate().toString()))
                .andExpect(jsonPath("$.data.clubs[1].endDate").value(club2.getEndDate().toString()))
                .andExpect(jsonPath("$.data.clubs[1].isPublic").value(club2.isPublic()))
                .andExpect(jsonPath("$.data.clubs[1].myRole").value("MANAGER"))
                .andExpect(jsonPath("$.data.clubs[1].myState").value("INVITED"));
    }

    @Test
    @DisplayName("내 클럽 목록 반환 - 클럽이 없는 경우 빈 목록 반환")
    @WithUserDetails(value ="uny@test.com") // 6번 멤버로 로그인
    public void getMyClubs_EmptyList() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/my-clubs")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("getMyClubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("내 클럽 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.clubs.length()").value(0)); // 빈 목록이어야 함
    }

    @Test
    @DisplayName("클럽 가입 신청 취소")
    @WithUserDetails(value = "uny@test.com") // 6번 멤버로 로그인
    public void cancelClubApplication() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 가입 신청 중인 멤버 추가 (6번을 가입 신청 상태로 추가)
        Member applyingMember = memberService.findMemberById(6L)
                .orElseThrow(() -> new IllegalStateException("가입 신청 중인 멤버가 존재하지 않습니다."));

        ClubMember applyingClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                applyingMember,
                ClubMemberRole.PARTICIPANT
        );

        applyingClubMember.updateState(ClubMemberState.APPLYING); // 가입 신청 상태로 업데이트

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/my-clubs/" + club.getId() + "/apply")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("cancelClubApplication"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 가입 신청을 취소했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증: 클럽 멤버 목록에서 가입 신청 중인 멤버가 제거되었는지 확인
        assertThat(club.getClubMembers().size()).isEqualTo(1); // 호스트 멤버만 남아 있어야 함
        assertThat(club.getClubMembers().get(0).getMember().getId()).isEqualTo(hostMember.getId());
    }

    @Test
    @DisplayName("클럽 가입 신청 취소 - 존재하지 않는 클럽")
    @WithUserDetails(value = "uny@test.com") // 6번 멤버로 로그인
    public void cancelClubApplication_InvalidClubId() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/my-clubs/999/apply") // 존재하지 않는 클럽 ID
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("cancelClubApplication"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("클럽 탈퇴")
    @WithUserDetails(value = "uny@test.com") // 6번 멤버로 로그인
    public void leaveClub() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (6번을 초대)
        Member invitedMember = memberService.findMemberById(6L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        ClubMember clubMember = clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );
        // 클럽 멤버 상태를 JOINING으로 업데이트
        clubMember.updateState(ClubMemberState.JOINING);
        clubMemberRepository.save(clubMember);

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/my-clubs/" + club.getId() + "/withdraw")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("withdrawFromClub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽에서 탈퇴했습니다."));

        // 추가 검증: 멤버가 실제로 제거되지 않고 상태가 WITHDRAWN으로 변경되었는지 확인
        ClubMember leftClubMember = clubMemberRepository.findById(clubMember.getId())
                .orElseThrow(() -> new IllegalStateException("탈퇴한 멤버가 존재하지 않습니다."));
        assertThat(leftClubMember.getState()).isEqualTo(ClubMemberState.WITHDRAWN);

    }



}