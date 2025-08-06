package com.back.domain.member.member;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.dto.request.GuestDto;
import com.back.domain.member.member.dto.request.MemberRegisterDto;
import com.back.domain.member.member.dto.response.MemberDetailInfoResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.member.support.MemberFixture;
import com.back.global.enums.MemberType;
import com.back.global.exception.ServiceException;
import com.back.global.security.SecurityUser;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1MemberControllerTest {
    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 - 정상 기입 / 객체 정상 생성")
    public void memberObjectCreationTest() {
        MemberInfo memberInfo = MemberInfo.builder()
                .email("qkqek6223@naver.com")
                .bio("안녕하세요 반갑습니다")
                .profileImageUrl("https://picsum.photos/seed/picsum/200/300")
                .build();

        Member member = Member.builder()
                .nickname("안수지")
                .password("password123")
                .memberInfo(memberInfo)
                .presets(null)
                .build();

        assertEquals("안수지", member.getNickname());
        assertEquals("password123", member.getPassword());
        assertNotNull(member.getMemberInfo());
        assertEquals("qkqek6223@naver.com", member.getMemberInfo().getEmail());
        assertEquals("안녕하세요 반갑습니다", member.getMemberInfo().getBio());
    }

    @Test
    @DisplayName("회원가입 - 정상 기입 / POST 정상 작동")
    public void memberPostTest() throws  Exception {
        String requestBody = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 기입 / POST 실패")
    public void memberPostTestException1() throws  Exception {
        String requestBody = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        String requestBody2 = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지1",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 - 이메일 형식 오류로 실패")
    public void registerWithInvalidEmailFormat() throws Exception {
        String requestBody = """
        {
            "email": "invalid-email-format",
            "password": "password123",
            "nickname": "userInvalidEmail",
            "bio": "bio"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("email-Email-이메일 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 - 비밀번호 미입력(빈값)으로 실패")
    public void registerWithBlankPassword() throws Exception {
        String requestBody = """
        {
            "email": "user@example.com",
            "password": "",
            "nickname": "userNoPassword",
            "bio": "bio"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("password-NotBlank-비밀번호는 필수 입력값입니다."));
    }

    @Test
    @DisplayName("회원가입 - 닉네임 누락으로 실패")
    public void registerWithMissingNickname() throws Exception {
        String requestBody = """
        {
            "email": "user@example.com",
            "password": "password123",
            "bio": "bio"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("nickname-NotBlank-닉네임은 필수 입력값입니다."));
    }




    @Test
    @DisplayName("API key 발급 - 정상")
    public void generateApiKey_success() throws Exception {
        String apiKey = apiKeyService.generateApiKey();

        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("api_"));
    }

    @Test
    @DisplayName("AccessToken 발급 - 정상")
    public void generateAccessToken_success() throws Exception {
        Member member = memberFixture.createMember(1);

        String accessToken = authService.generateAccessToken(member);

        assertNotNull(accessToken);
    }

    @Test
    @DisplayName("로그인 - 정상 기입")
    public void loginSuccess() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "test1@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.apikey").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 - 없는 이메일 기입")
    public void loginNonexistentEmail() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "wrong@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    @DisplayName("로그인 - 맞지 않는 비밀번호 기입")
    public void loginWrongPassword() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "test1@example.com",
            "password": "WrongPassword"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    @DisplayName("로그인 실패 - 이메일 대소문자 구분 (대문자 입력 시도)")
    public void loginFail_emailCaseSensitive() throws Exception {
        memberFixture.createMember(1); // test1@example.com 으로 회원 생성됨

        String requestBody = """
    {
        "email": "TEST1@EXAMPLE.COM",
        "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 공백 입력")
    public void loginFail_blankPassword() throws Exception {
        memberFixture.createMember(1); // 회원 생성

        String requestBody = """
    {
        "email": "test1@example.com",
        "password": ""
    }
    """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())  // 빈 비밀번호는 보통 400 Bad Request 처리 예상
                .andExpect(jsonPath("$.message").value("password-NotBlank-비밀번호는 필수 입력값입니다.")) // DTO @NotBlank 메시지와 일치하게 수정
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());
    }


    @Test
    @DisplayName("로그아웃 - 정상 처리")
    @WithUserDetails(value = "hgd222@test.com")
    public void logout() throws Exception {
        memberFixture.createMember(1);

        Cookie accessTokenCookie = loginAndGetAccessTokenCookie("test1@example.com", "password123");

        mockMvc.perform(delete("/api/v1/members/auth/logout")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0)); // 쿠키 만료 확인
    }

    @Test
    @DisplayName("회원탈퇴 - 정상 처리")
    public void withdrawMembership() throws Exception {
        Member member = memberFixture.createMember(1);

        Cookie accessTokenCookie = loginAndGetAccessTokenCookie("test1@example.com", "password123");

        mockMvc.perform(delete("/api/v1/members/me")
                .with(user(new SecurityUser(
                        member.getId(),
                        member.getNickname(),
                        member.getTag(),
                        member.getMemberType(),
                        member.getPassword(),
                        Collections.emptyList()
                )))
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.tag").value(member.getTag()))
                .andExpect(cookie().maxAge("accessToken", 0)); // 쿠키 만료 확인

        Optional<Member> deletedMember = memberRepository.findById(member.getId());
        assertThat(deletedMember).isEmpty();
    }

    @Test
    @DisplayName("회원탈퇴 - 인증 없이 탈퇴 요청 시도")
    public void withdrawMembership_Unauthenticated_Failure() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("회원탈퇴 - 존재하지 않는 회원 ID로 탈퇴 시도 시 404 Not Found")
    public void withdrawMembership_NonexistentUserId_Failure() throws Exception {
        // 실제 DB에 없는 임의의 회원 ID 사용
        Long fakeMemberId = 999999L;

        SecurityUser fakeUser = new SecurityUser(
                fakeMemberId,
                "fakeNickname",
                "fakeTag",
                MemberType.MEMBER,
                "fakePassword",
                Collections.emptyList()
        );

        mockMvc.perform(delete("/api/v1/members/me")
                        .with(user(fakeUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("회원 정보를 찾을 수 없습니다."));
    }


    @Test
    @DisplayName("access token 재발급 - 정상")
    @WithUserDetails(value = "hgd222@test.com")
    public void reGenerateAccessToken_success() throws Exception {
        //회원 생성
        Member member = memberFixture.createMember(1);

        //로그인하여 액세스 토큰 쿠키 받기
        loginAndGetAccessTokenCookie("test1@example.com", "password123");

        //apiKey 멤버에서 가져오기
        String apiKey = member.getMemberInfo().getApiKey();

        //액세스 토큰 재발급 요청 바디
        String requestBody = String.format("""
        {
            "refreshToken": "%s"
        }
        """, apiKey);

        //재발급 api 호출 및 검증
        mockMvc.perform(post("/api/v1/members/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Access Token 재발급 성공"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.apikey").value(apiKey))
                .andExpect(cookie().exists("accessToken"))
                .andDo(print());
    }

    @Test
    @DisplayName("Access Token 재발급 실패 - 잘못된 또는 만료된 refreshToken")
    @WithUserDetails(value = "hgd222@test.com")
    public void reGenerateAccessToken_fail_invalidOrExpiredRefreshToken() throws Exception {
        String invalidRefreshToken = "invalid_or_expired_refresh_token";

        String requestBody = String.format("""
    {
        "refreshToken": "%s"
    }
    """, invalidRefreshToken);

        mockMvc.perform(post("/api/v1/members/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token 입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 실패 - 잘못된 accessToken 으로 요청")
    @WithUserDetails(value = "hgd222@test.com")
    public void logout_fail_invalidAccessToken() throws Exception {
        Cookie invalidAccessTokenCookie = new Cookie("accessToken", "invalid_access_token_value");

        mockMvc.perform(delete("/api/v1/members/auth/logout")
                        .cookie(invalidAccessTokenCookie))
                .andExpect(jsonPath("$.code").value(499))
                .andExpect(jsonPath("$.message").value("access token이 유효하지 않습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }



    @Test
    @DisplayName("내 정보 조회 - 정상")
    public void getMyInfo_success() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(get("/api/v1/members/me")
                        .with(user(securityUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("유저 정보 반환 성공"))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.email").value(memberInfo.getEmail()))
                .andExpect(jsonPath("$.data.profileImage").value(memberInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.data.bio").value(memberInfo.getBio()));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 정상")
    public void checkPasswordValidity_success() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();
        String rawPassword = "password123"; //평문 비밀번호

        String requestBody = String.format("""
        {
            "password": "%s"
        }
        """, rawPassword);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), rawPassword))
                    .with(user(securityUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호 유효성 반환 성공"))
                .andExpect(jsonPath("$.data.verified").value(true))    ;

    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 잘못된 비밀번호")
    public void checkPasswordValidity_wrongPassword() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();
        String wrongPassword = "wrongPassword!"; // 틀린 비밀번호

        String requestBody = String.format("""
    {
        "password": "%s"
    }
    """, wrongPassword);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), "password123")) // 실제 맞는 비밀번호로 로그인
                        .with(user(securityUser)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.verified").value(false));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 인증되지 않은 사용자")
    public void checkPasswordValidity_unauthorized() throws Exception {
        String requestBody = """
    {
        "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 빈 비밀번호")
    public void checkPasswordValidity_blankPassword() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();

        String requestBody = """
    {
        "password": ""
    }
    """;

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), "password123"))
                        .with(user(securityUser)))
                .andExpect(status().isBadRequest())  // @NotBlank 검증 실패 예상
                .andExpect(jsonPath("$.code").value(400)); // 상세 메시지는 DTO의 @NotBlank 메시지에 따라 다름
    }

    @Test
    @DisplayName("유저 정보 수정 - 성공")
    public void updateUserInfoTest_green() throws Exception {
        Member member = memberFixture.createMember(1);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        MemberDetailInfoResponse response = new MemberDetailInfoResponse(
                "개나리", "test1@example.com", "노란색 개나리", "http://s3.com/profile.jpg", "newTag");


        String requestBody = """
                {
                    "nickname": "개나리",
                    "password": "newPassword",
                    "bio": "노란색 개나리"
                }
                """;

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data",
                MediaType.APPLICATION_JSON_VALUE,
                requestBody.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "profileImage", "profileImage",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/members/me")
                        .file(dataPart)
                        .file(imagePart)
                        .with(user(securityUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("개나리"))
                .andExpect(jsonPath("$.data.bio").value("노란색 개나리"))
                .andExpect(jsonPath("$.data.profileImage").isNotEmpty());

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("개나리");
        assertThat(updated.getTag()).isNotNull();
        assertThat(updated.getPassword()).isNotEqualTo("newPassword");
        assertThat(updated.getMemberInfo().getBio()).isEqualTo("노란색 개나리");
        assertThat(updated.getMemberInfo().getProfileImageUrl()).isNotBlank();
    }

    @Test
    @DisplayName("회원 정보 수정 - 잘못된 multipart 형식으로 요청 (data part 누락)")
    public void updateUserInfo_invalidMultipartFormat() throws Exception {
        Member member = memberFixture.createMember(1);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        // data part 없이 프로필 이미지 파일만 보냄 → 예외 발생 예상
        MockMultipartFile imagePart = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/members/me")
                        .file(imagePart)
                        .with(user(securityUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("필수 multipart 파트 'data'가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 - 허용되지 않은 이미지 포맷 업로드 시 400 Bad Request")
    public void updateUserInfo_invalidImageFormat() throws Exception {
        Member member = memberFixture.createMember(1);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        String requestBody = """
            {
                "nickname": "개나리",
                "password": "newPassword",
                "bio": "노란색 개나리"
            }
            """;

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data",
                MediaType.APPLICATION_JSON_VALUE,
                requestBody.getBytes(StandardCharsets.UTF_8)
        );

        // 허용되지 않는 파일 확장자 (예: .txt)
        MockMultipartFile invalidImagePart = new MockMultipartFile(
                "profileImage", "profileImage.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "some text content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/members/me")
                        .file(dataPart)
                        .file(invalidImagePart)
                        .with(user(securityUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @DisplayName("비회원 모임 등록 - 성공 및 DB 저장 확인")
    public void GuestRegister_success_withDbCheck() throws Exception {
        // 회원 생성 (기존 회원 fixture)
        Member guest = memberFixture.createMember(1);

        // API 요청 바디 (요청하는 비회원 정보)
        String nickname = "guestUser";
        String rawPassword = "guestPassword123";
        Long clubId = 1L;
        Club club = clubRepository.findById(clubId).orElseThrow();
        String requestBody = String.format("""
        {
            "nickname": "%s",
            "password": "%s",
            "clubId": %d
        }
        """, nickname, rawPassword, clubId);

        // API 호출 및 응답 검증
        mockMvc.perform(post("/api/v1/members/auth/guest-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비회원 모임 가입 성공"))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.clubId").value(clubId))
                .andExpect(cookie().exists("accessToken"));

        // DB에서 저장 여부 확인
        Optional<Member> savedGuestOpt = memberRepository.findByNickname(nickname);
        assertTrue(savedGuestOpt.isPresent(), "비회원 게스트 회원이 멤버 DB에 저장되어야 합니다.");

        Member savedGuest = savedGuestOpt.get();
        Optional<ClubMember> savedClubGuestOpt = clubMemberRepository.findByClubAndMember(club, savedGuest);
        assertTrue(savedClubGuestOpt.isPresent(), "비회원 게스트 회원이 클럽멤버 DB에 저장되어야 합니다.");

        assertEquals(nickname, savedGuest.getNickname());
        assertEquals(MemberType.GUEST, savedGuest.getMemberType());

        // 비밀번호는 암호화되어 저장되었을 것이므로, 평문과 다를 것
        assertNotEquals(rawPassword, savedGuest.getPassword());

        // tag가 자동 생성되거나 세팅된다면, null이 아닌지 확인 가능
        assertNotNull(savedGuest.getTag());
    }

    @Test
    @DisplayName("비회원 임시 로그인 - 정상 처리")
    public void guestLogin_success() throws Exception {
        Member guest = memberRepository.findByNickname("김암호").orElseThrow();

        Club club = clubRepository.findAll().stream()
                .filter(c -> c.getName().equals("친구 모임2"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("친구 모임2 클럽을 찾을 수 없습니다."));
        System.out.println("친구 모임2 ID = " + club.getId());

        String rawPassword = "password13";

        String requestBody = """
        {
            "nickname": "%s",
            "password": "%s",
            "clubId": %d
        }
    """.formatted(guest.getNickname(), rawPassword, club.getId());

        mockMvc.perform(post("/api/v1/members/auth/guest-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비회원 로그인 성공"))
                .andExpect(jsonPath("$.data.nickname").value(guest.getNickname()))
                .andExpect(jsonPath("$.data.clubId").value(club.getId().intValue()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("accessToken"))
                .andDo(print());
    }

    @Test
    @DisplayName("비회원 닉네임 중복 확인 - 중복된 경우")
    void guestNicknameDuplicate_shouldReturnTrue() throws Exception {
        // given
        GuestDto guestDto = new GuestDto("중복회원", "password", 5L);

        memberService.registerGuestMember(guestDto);

        String duplicateNickname = "중복회원";

        String requestBody = """
        {
            "nickname": "%s",
            "password": "password12",
            "clubId": 5
        }
    """.formatted(duplicateNickname);

        // when & then
        mockMvc.perform(post("/api/v1/members/auth/guest-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }



    @Test
    @DisplayName("회원가입 - 이메일 중복 시 예외 발생")
    public void registerWithDuplicateEmailThrowsException() {
        MemberRegisterDto memberRegisterDto1 = new MemberRegisterDto("1", "pw1", "user1", "안녕하세요");
        MemberRegisterDto memberRegisterDto2 = new MemberRegisterDto("1", "pw1", "user2", "안녕하세요");

        memberService.registerMember(memberRegisterDto1);

        assertThatThrownBy(() -> {
            memberService.registerMember(memberRegisterDto2);
        }).isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("회원가입 - 비밀번호 해싱 성공")
    public void registerPasswordHashingAndMatching() {
        String rawPassword = "pw1";

        memberService.registerMember(new MemberRegisterDto("1", rawPassword, "user1", "<>"));
        Member savedMember = memberRepository.findByNickname("user1").get();

        String savedHashedPassword = savedMember.getPassword();

        assertNotEquals(rawPassword, savedHashedPassword);

        assertTrue(passwordEncoder.matches(rawPassword, savedHashedPassword));

        assertFalse(passwordEncoder.matches("wrongPassword", savedHashedPassword));
    }

    @Test
    @DisplayName("로그인 - 액세스토큰에 tag, memberType 포함 확인")
    public void accessToken_containsTagAndMemberType() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "test1@example.com",
            "password": "password123"
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        // 액세스토큰 문자열 추출
        String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");

        // JWT 구조: header.payload.signature -> 우리는 payload만 디코딩
        String[] parts = accessToken.split("\\.");
        assertEquals(3, parts.length, "JWT 형식이 아님");

        // payload(base64) 디코딩 후 JSON 문자열로 변환
        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

        // JSON 파싱 없이 문자열 포함 여부만 확인해도 기본적인 검증 가능
        assertTrue(payloadJson.contains("\"tag\":\""), "토큰에 tag 포함 안됨");
        assertTrue(payloadJson.contains("\"memberType\":\"MEMBER\""), "토큰에 memberType 포함 안됨");
    }





    private Cookie loginAndGetAccessTokenCookie(String email, String password) throws Exception {
        String loginRequestBody = String.format("""
        {
            "email": "%s",
            "password": "%s"
        }
        """, email, password);

        return  mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }
}
