package com.back.domain.club.clubLink.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.club.clubLink.service.ClubLinkService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1ClubLinkControllerTest {

    @Autowired
    private ClubLinkService clubLinkService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ClubLinkRepository clubLinkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("초대 링크 생성 - 링크 생성 성공")
    @WithUserDetails(value = "hgd222@test.com")
        // 1번 멤버로 로그인
    void createClubLink_Success() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대 링크가 생성되었습니다."))
                .andExpect(jsonPath("$.data.link").exists())
                .andExpect(jsonPath("$.data.link").value(org.hamcrest.Matchers.containsString("https://supplies.com/clubs/invite?token=")))
                .andReturn();

        // 응답 JSON 에서 link 추출
        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(responseBody);
        String link = root.path("data").path("link").asText();
        String[] urlParts = link.split("\\?");
        if (urlParts.length > 1) {
            String[] params = urlParts[1].split("&");

            String inviteCodeFromResponse = null;
            for (String param : params) {
                if (param.startsWith("token=")) {
                    inviteCodeFromResponse = param.substring(6);
                    break;
                }
            }

            assertNotNull(inviteCodeFromResponse);

            // DB 에서 실제 저장된 초대 코드 확인
            Club club = clubRepository.findById(1L).orElseThrow();
            Optional<ClubLink> savedLink = clubLinkRepository.findByClubAndExpiresAtAfter(club, LocalDateTime.now());
            assertTrue(savedLink.isPresent());
            assertEquals(inviteCodeFromResponse, savedLink.get().getInviteCode());
        }
    }

    @Test
    @DisplayName("초대 링크 생성 실패 - 존재하지 않는 클럽 ID")
    @WithUserDetails(value = "hgd222@test.com")
    void createClubLink_Fail_ClubNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/clubs/9999999/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("해당 id의 클럽을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("초대 링크 생성 실패 - 권한 없는 멤버")
    @WithUserDetails(value = "lyh3@test.com")
    void createClubLink_Fail_NoPermission() throws Exception {
        mockMvc.perform(post("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("호스트나 매니저만 초대 링크를 관리할 수 있습니다."));
    }

    @Test
    @DisplayName("초대 링크 생성 - 기존 유효한 링크가 존재할 경우 해당 링크 반환")
    @WithUserDetails("hgd222@test.com")
        // HOST 또는 MANAGER 권한을 가진 사용자
    void createClubLink_ExistingLink_Returned() throws Exception {
        // given
        Club club = clubRepository.findById(1L).orElseThrow();

        // 이미 존재하는 링크 저장
        String existingCode = "existing-code-123";
        ClubLink existingLink = ClubLink.builder()
                .inviteCode(existingCode)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(7)) // 아직 유효한 링크
                .club(club)
                .build();

        clubLinkRepository.save(existingLink);

        // when
        mockMvc.perform(post("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대 링크가 생성되었습니다."))
                .andExpect(jsonPath("$.data.link").value(org.hamcrest.Matchers.containsString(existingCode)));
    }

    @Test
    @DisplayName("초대 링크 조회 성공 - 유효한 초대 링크가 존재하면 반환")
    @WithUserDetails("hgd222@test.com")
        // HOST 또는 MANAGER
    void getExistingClubLink_success() throws Exception {
        // given
        Club club = clubRepository.findById(1L).orElseThrow();
        String inviteCode = "valid-code-456";

        ClubLink clubLink = ClubLink.builder()
                .inviteCode(inviteCode)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        // when & then
        mockMvc.perform(get("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대 링크가 반환되었습니다."))
                .andExpect(jsonPath("$.data.link").value(org.hamcrest.Matchers.containsString(inviteCode)));
    }

    @Test
    @DisplayName("초대 링크 조회 실패 - 유효한 링크가 없을 때 예외 발생")
    @WithUserDetails("hgd222@test.com")
    void getExistingClubLink_fail_noLink() throws Exception {
        // given
        Club club = clubRepository.findById(1L).orElseThrow();
        clubLinkRepository.deleteAll(); // 링크 제거

        // when & then
        mockMvc.perform(get("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("활성화된 초대 링크를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("초대 링크 조회 실패 - 권한 없는 멤버일 경우 예외 발생")
    @WithUserDetails("lyh3@test.com")// MEMBER 권한만 있음
    void getExistingClubLink_fail_noPermission() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("호스트나 매니저만 초대 링크를 관리할 수 있습니다."));
    }

    @Test
    @DisplayName("초대 링크 조회 실패 - 존재하지 않는 클럽 ID")
    @WithUserDetails("hgd222@test.com")
    void getExistingClubLink_fail_clubNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/9999999/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("해당 id의 클럽을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("초대 링크 가입 신청 성공 - 유효한 토큰, 미가입자")
    @WithUserDetails("lcw@test.com")
    void applyToPrivateClub_success() throws Exception {
        Club club = clubRepository.findById(2L).orElseThrow(); // 2L: "친구 모임"

        ClubLink clubLink = ClubLink.builder()
                .inviteCode("valid-token-123")
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        mockMvc.perform(post("/api/v1/clubs/invitations/valid-token-123/apply"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 가입 신청이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("초대 링크 실패 - 유효하지 않은 토큰")
    @WithUserDetails("hgd222@test.com")
    void applyToPrivateClub_fail_invalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/clubs/invitations/invalid-token/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("초대 토큰이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("초대 링크 실패 - 토큰 만료")
    @WithUserDetails("hgd222@test.com")
    void applyToPrivateClub_fail_expiredToken() throws Exception {
        Club club = clubRepository.findById(2L).orElseThrow(); // "친구 모임"

        ClubLink clubLink = ClubLink.builder()
                .inviteCode("expired-token-456")
                .createdAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        mockMvc.perform(post("/api/v1/clubs/invitations/expired-token-456/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("초대 토큰이 만료되었습니다."));
    }

    @Test
    @DisplayName("초대 링크 실패 - 이미 가입 상태 (JOINING)")
    @WithUserDetails("chs4s@test.com")
        // 김철수 로그인
    void applyToPrivateClub_fail_alreadyJoined() throws Exception {
        Club club = clubRepository.findById(2L).orElseThrow(); // "친구 모임"
        Member member = findMemberByEmail("chs4s@test.com");

        ClubLink clubLink = ClubLink.builder()
                .inviteCode("joined-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        ClubMember joinedMember = ClubMember.builder()
                .member(member)
                .club(club)
                .role(ClubMemberRole.PARTICIPANT)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(joinedMember);

        mockMvc.perform(post("/api/v1/clubs/invitations/joined-token/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 이 클럽에 가입되어 있습니다."));
    }

    @Test
    @DisplayName("초대 링크 실패 - 이미 신청 상태 (APPLYING)")
    @WithUserDetails("lcw@test.com")
        // 이채원 로그인
    void applyToPrivateClub_fail_applying() throws Exception {
        Club club = clubRepository.findById(2L).orElseThrow(); // "친구 모임"
        Member member = findMemberByEmail("lcw@test.com");

        ClubLink clubLink = ClubLink.builder()
                .inviteCode("applying-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        ClubMember applyingMember = ClubMember.builder()
                                .member(member)
                                .club(club)
                                .role(ClubMemberRole.PARTICIPANT)
                                .state(ClubMemberState.APPLYING)
                                .build();
                clubMemberRepository.save(applyingMember);

        mockMvc.perform(post("/api/v1/clubs/invitations/applying-token/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 가입 신청 중입니다."));
    }

    @Test
    @DisplayName("초대 링크 실패 - 이미 초대 상태 (INVITED)")
    @WithUserDetails("hyh@test.com")
    void applyToPrivateClub_fail_invited() throws Exception {
        Club club = clubRepository.findById(2L).orElseThrow();
        Member member = findMemberByEmail("hyh@test.com");

        ClubLink clubLink = ClubLink.builder()
                .inviteCode("invited-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        ClubMember invitedMember = ClubMember.builder()
                .member(member)
                .club(club)
                .role(ClubMemberRole.PARTICIPANT)
                .state(ClubMemberState.INVITED)
                .build();
        clubMemberRepository.save(invitedMember);

        mockMvc.perform(post("/api/v1/clubs/invitations/invited-token/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 초대를 받은 상태입니다. 마이페이지에서 수락해주세요."));
    }

    @Test
    @DisplayName("초대 링크 조회 성공 - 유효한 토큰")
    @WithUserDetails("uny@test.com")
    void getClubInfoByInvitationToken_success() throws Exception {
        // given
        Club club = clubRepository.findById(2L).orElseThrow(); // 친구 모임
        String token = "token-success-123";

        ClubLink clubLink = ClubLink.builder()
                .inviteCode(token)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        // when & then
        mockMvc.perform(get("/api/v1/clubs/invitations/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 정보가 반환되었습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.name").value(club.getName()))
                .andExpect(jsonPath("$.data.imageUrl").value(club.getImageUrl()))
                .andExpect(jsonPath("$.data.mainSpot").value(club.getMainSpot()))
                .andExpect(jsonPath("$.data.eventType").value(club.getEventType().name()))
                .andExpect(jsonPath("$.data.startDate").value(club.getStartDate().toString()))
                .andExpect(jsonPath("$.data.endDate").value(club.getEndDate().toString()))
                .andExpect(jsonPath("$.data.leaderId").value(club.getLeaderId()));
    }

    @Test
    @DisplayName("초대 링크 조회 실패 - 존재하지 않는 토큰")
    @WithUserDetails("uny@test.com")
    void getClubInfoByInvitationToken_fail_invalidToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/clubs/invitations/invalid-token-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("초대 토큰이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("초대 링크 조회 실패 - 만료된 토큰")
    @WithUserDetails("uny@test.com")
    void getClubInfoByInvitationToken_fail_expiredToken() throws Exception {
        // given
        Club club = clubRepository.findById(2L).orElseThrow();
        String token = "expired-token-789";

        ClubLink clubLink = ClubLink.builder()
                .inviteCode(token)
                .createdAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료됨
                .club(club)
                .build();
        clubLinkRepository.save(clubLink);

        // when & then
        mockMvc.perform(get("/api/v1/clubs/invitations/{token}", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("초대 토큰이 만료되었습니다."));
    }


    //================기타 메서드========================

    private Member findMemberByEmail(String email) {
        return memberRepository.findByMemberInfo_Email(email)
                            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + email));    }
}
