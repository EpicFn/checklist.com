package com.back.domain.club.club.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.dto.request.MemberRegisterDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1ClubControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClubService clubService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ClubMemberService clubMemberService;

    @MockitoBean
    private S3Service s3Service; // S3Service는 MockBean으로 주입하여 실제 S3와의 통신을 피합니다.

    @Test
    @DisplayName("빈 클럽 생성 - 이미지 없는 경우")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void createClub() throws Exception {
        // given
        String jsonData = """
            {
                "name": "테스트 그룹",
                "bio": "테스트 그룹 설명",
                "category" : "TRAVEL",
                "mainSpot" : "서울",
                "maximumCapacity" : 10,
                "eventType" : "SHORT_TERM",
                "startDate" : "2023-10-01",
                "endDate" : "2023-10-31",
                "isPublic": true,
                "clubMembers" : []
            }
            """;

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/clubs").file(dataPart)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("createClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽이 생성됐습니다."))
                .andExpect(jsonPath("$.data.clubId").isNumber())
                .andExpect(jsonPath("$.data.leaderId").value(1));

        // 추가 검증: 그룹이 실제로 생성되었는지 확인
        Club club = clubService.getLastCreatedClub();

        assertThat(club.getName()).isEqualTo("테스트 그룹");
        assertThat(club.getBio()).isEqualTo("테스트 그룹 설명");
        assertThat(club.getCategory()).isEqualTo(ClubCategory.TRAVEL);
        assertThat(club.getMainSpot()).isEqualTo("서울");
        assertThat(club.getMaximumCapacity()).isEqualTo(10);
        assertThat(club.getEventType()).isEqualTo(EventType.SHORT_TERM);
        assertThat(club.getStartDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(club.getEndDate()).isEqualTo(LocalDate.of(2023, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(1L);
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().size()).isEqualTo(1); // 구성원이 한명(호스트)인지 확인
    }

    @Test
    @DisplayName("빈 클럽 생성 - 이미지가 있는 경우")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void createClubWithImage() throws Exception {
        // given
        // ⭐️ S3 업로더의 행동 정의: 어떤 파일이든 업로드 요청이 오면, 지정된 가짜 URL을 반환한다.
        String fakeImageUrl = "https://my-s3-bucket.s3.ap-northeast-2.amazonaws.com/club/1/profile/fake-image.jpg";
        given(s3Service.upload(any(MultipartFile.class), any(String.class))).willReturn(fakeImageUrl);

        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성 (위와 동일)
        String jsonData = """
            {
                "name": "이미지 있는 그룹",
                "bio": "테스트 그룹 설명",
                "category" : "HOBBY",
                "mainSpot" : "부산",
                "maximumCapacity" : 5,
                "eventType" : "LONG_TERM",
                "startDate" : "2025-08-01",
                "endDate" : "2026-07-31",
                "isPublic": false,
                "clubMembers" : []
            }
            """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));


        // when
        // 3. MockMvc로 multipart 요청 생성 (JSON 파트와 이미지 파트 모두 포함)
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs")
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.clubId").isNumber());

        // 추가 검증
        Club club = clubService.getLastCreatedClub();
        assertThat(club.getName()).isEqualTo("이미지 있는 그룹");
        assertThat(club.getImageUrl()).isEqualTo(fakeImageUrl); // ⭐️ 이미지 URL이 가짜 URL과 일치하는지 확인
    }

    @Test
    @DisplayName("초기 유저 있는 클럽 생성")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void createClubWithMembers() throws Exception {
        // given
        String jsonData = """
            {
                "name": "테스트 그룹",
                "bio": "테스트 그룹 설명",
                "category" : "TRAVEL",
                "mainSpot" : "서울",
                "maximumCapacity" : 10,
                "eventType" : "SHORT_TERM",
                "startDate" : "2023-10-01",
                "endDate" : "2023-10-31",
                "isPublic": true,
                "clubMembers" : [
                    {
                        "id": 2,
                        "role" : "MANAGER"
                    },
                    {
                        "id": 3,
                        "role" : "PARTICIPANT"
                    },
                    {
                        "id": 4,
                        "role" : "PARTICIPANT"
                    }
                ]
            }
            """;

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/clubs").file(dataPart)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("createClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽이 생성됐습니다."))
                .andExpect(jsonPath("$.data.clubId").isNumber())
                .andExpect(jsonPath("$.data.leaderId").value(1));

        // 추가 검증: 클럽이 실제로 생성되었는지 확인
        Club club = clubService.getLastCreatedClub();

        assertThat(club.getName()).isEqualTo("테스트 그룹");
        assertThat(club.getBio()).isEqualTo("테스트 그룹 설명");
        assertThat(club.getCategory()).isEqualTo(ClubCategory.TRAVEL);
        assertThat(club.getMainSpot()).isEqualTo("서울");
        assertThat(club.getMaximumCapacity()).isEqualTo(10);
        assertThat(club.getEventType()).isEqualTo(EventType.SHORT_TERM);
        assertThat(club.getStartDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(club.getEndDate()).isEqualTo(LocalDate.of(2023, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(1L);
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().size()).isEqualTo(4); // 클럽 멤버가 4명인지 확인

        // 클럽 멤 검증
        assertThat(club.getClubMembers().get(0).getRole().name()).isEqualTo("HOST");
        assertThat(club.getClubMembers().get(0).getMember().getId()).isEqualTo(1L);
        assertThat(club.getClubMembers().get(1).getRole().name()).isEqualTo("MANAGER");
        assertThat(club.getClubMembers().get(1).getMember().getId()).isEqualTo(2L);
        assertThat(club.getClubMembers().get(2).getRole().name()).isEqualTo("PARTICIPANT");
        assertThat(club.getClubMembers().get(2).getMember().getId()).isEqualTo(3L);
        assertThat(club.getClubMembers().get(3).getRole().name()).isEqualTo("PARTICIPANT");
        assertThat(club.getClubMembers().get(3).getMember().getId()).isEqualTo(4L);

    }

    @Test
    @DisplayName("클럽 정보 수정")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void updateClub() throws Exception {
        // given
        // 클럽 생성
        Long clubId = 1L; // 테스트를 위해 클럽 ID를 1로 고정

        Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new IllegalStateException("클럽이 존재하지 않습니다."));

        // ⭐️ S3 업로더의 행동 정의: 어떤 파일이든 업로드 요청이 오면, 지정된 가짜 URL을 반환한다.
        String fakeImageUrl = "https://my-s3-bucket.s3.ap-northeast-2.amazonaws.com/club/1/profile/fake-image.jpg";
        given(s3Service.upload(any(MultipartFile.class), any(String.class))).willReturn(fakeImageUrl);

        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성 (위와 동일)
        String jsonData = """
            {
                "name": "수정된 테스트 그룹",
                "bio": "수정된 테스트 그룹 설명",
                "category" : "HOBBY",
                "mainSpot" : "수정된 서울",
                "maximumCapacity" : 11,
                "recruitingStatus": false,
                "eventType" : "LONG_TERM",
                "startDate" : "2024-10-01",
                "endDate" : "2024-10-31",
                "isPublic": true
            }
            """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));


        // when
        // 3. MockMvc로 multipart 요청 생성 (JSON 파트와 이미지 파트 모두 포함)
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs/" + club.getId()) // 클럽 ID를 URL에 포함
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                                .with(request -> {
                                    request.setMethod("PATCH"); // PATCH 메소드로 요청
                                    return request;
                                })
                )
                .andDo(print());


        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("updateClubInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 정보가 수정됐습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()));


        // 추가 검증: 클럽이 실제로 수정되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getName()).isEqualTo("수정된 테스트 그룹");
        assertThat(club.getBio()).isEqualTo("수정된 테스트 그룹 설명");
        assertThat(club.getCategory()).isEqualTo(ClubCategory.HOBBY);
        assertThat(club.getMainSpot()).isEqualTo( "수정된 서울");
        assertThat(club.getMaximumCapacity()).isEqualTo(11);
        assertThat(club.getImageUrl()).isEqualTo(fakeImageUrl); // 이미지 URL이 가짜 URL과 일치하는지 확인
        assertThat(club.getEventType()).isEqualTo(EventType.LONG_TERM);
        assertThat(club.getStartDate()).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(club.getEndDate()).isEqualTo(LocalDate.of(2024, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(1L);
        assertThat(club.isRecruitingStatus()).isFalse(); // 모집 상태가 false인지 확인
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("클럽 정보 수정 - 부분 수정")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void updateClubPart() throws Exception {
        // given
        // 클럽 생성
        Long clubId = 1L; // 테스트를 위해 클럽 ID를 1로 고정
        Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new IllegalStateException("클럽이 존재하지 않습니다."));

        String originalBio = club.getBio(); // 원래 bio 값 저장
        ClubCategory originalCategory = club.getCategory(); // 원래 category 값 저장
        String originalMainSpot = club.getMainSpot(); // 원래 mainSpot 값 저장
        EventType originalEventType = club.getEventType(); // 원래 eventType 값 저장
        Long originalLeaderId = club.getLeaderId(); // 원래 leaderId 값 저장


        // ⭐️ S3 업로더의 행동 정의: 어떤 파일이든 업로드 요청이 오면, 지정된 가짜 URL을 반환한다.
        String fakeImageUrl = "https://my-s3-bucket.s3.ap-northeast-2.amazonaws.com/club/1/profile/fake-image.jpg";
        given(s3Service.upload(any(MultipartFile.class), any(String.class))).willReturn(fakeImageUrl);

        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성 (위와 동일)
        String jsonData = """
            {
                "name": "수정된 테스트 그룹",
                "maximumCapacity" : 11,
                "recruitingStatus": false,
                "startDate" : "2024-10-01",
                "endDate" : "2024-10-31",
                "isPublic": true
            }
            """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));


        // when
        // 3. MockMvc로 multipart 요청 생성 (JSON 파트와 이미지 파트 모두 포함)
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs/" + club.getId()) // 클럽 ID를 URL에 포함
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                                .with(request -> {
                                    request.setMethod("PATCH"); // PATCH 메소드로 요청
                                    return request;
                                })
                )
                .andDo(print());


        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("updateClubInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 정보가 수정됐습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()));


        // 추가 검증: 클럽이 실제로 수정되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getName()).isEqualTo("수정된 테스트 그룹");
        assertThat(club.getBio()).isEqualTo(originalBio); // bio는 수정하지 않았으므로 원래 값과 동일
        assertThat(club.getCategory()).isEqualTo(originalCategory); // category는 수정하지 않았으므로 원래 값과 동일
        assertThat(club.getMainSpot()).isEqualTo(originalMainSpot); // mainSpot은 수정하지 않았으므로 원래 값과 동일
        assertThat(club.getMaximumCapacity()).isEqualTo(11);
        assertThat(club.getImageUrl()).isEqualTo(fakeImageUrl); // 이미지 URL이 가짜 URL과 일치하는지 확인
        assertThat(club.getEventType()).isEqualTo(originalEventType); // eventType은 수정하지 않았으므로 원래 값과 동일
        assertThat(club.getStartDate()).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(club.getEndDate()).isEqualTo(LocalDate.of(2024, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(originalLeaderId); // leaderId는 수정하지 않았으므로 원래 값과 동일
        assertThat(club.isRecruitingStatus()).isFalse(); // 모집 상태가 false인지 확인
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("클럽 수정 - 존재하지 않는 클럽")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void updateNonExistentClub() throws Exception {
        // given
        Long nonExistentClubId = 999L; // 존재하지 않는 클럽 ID

        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성
        String jsonData = """
            {
                "name": "수정된 테스트 그룹",
                "bio": "수정된 테스트 그룹 설명",
                "category" : "HOBBY",
                "mainSpot" : "수정된 서울",
                "maximumCapacity" : 11,
                "eventType" : "LONG_TERM",
                "startDate" : "2024-10-01",
                "endDate" : "2024-10-31",
                "isPublic": true
            }
            """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));

        // when
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs/" + nonExistentClubId) // 존재하지 않는 클럽 ID로 요청
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                                .with(request -> {
                                    request.setMethod("PATCH"); // PATCH 메소드로 요청
                                    return request;
                                })
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("모임을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("클럽 수정 - 권한 없는 유저")
    @WithUserDetails(value = "lyh3@test.com") //3번 유저로 로그인
    void updateClubWithoutPermission() throws Exception {
        // given
        // 클럽 생성
        Long clubId = 1L; // 테스트를 위해 클럽 ID를 1로 고정
        Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new IllegalStateException("클럽이 존재하지 않습니다."));


        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성
        String jsonData = """
                {
                    "name": "수정된 테스트 그룹",
                    "bio": "수정된 테스트 그룹 설명",
                    "category" : "HOBBY",
                    "mainSpot" : "수정된 서울",
                    "maximumCapacity" : 11,
                    "eventType" : "LONG_TERM",
                    "startDate" : "2024-10-01",
                    "endDate" : "2024-10-31",
                    "isPublic": true
                }
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));

        // when
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs/" + club.getId()) // 클럽 ID를 URL에 포함
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                                .with(request -> {
                                    request.setMethod("PATCH"); // PATCH 메소드로 요청
                                    return request;
                                })
                )
                .andDo(print());
        // then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("클럽 정보 삭제")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void deleteClub() throws Exception {
        // given
        // 클럽 생성
        Long clubId = 1L; // 테스트를 위해 클럽 ID를 1로 고정
        Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new IllegalStateException("클럽이 존재하지 않습니다."));


        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/" + club.getId())
                        .with(request -> {
                            request.setMethod("DELETE"); // DELETE 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("deleteClub"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("클럽이 삭제됐습니다."));

        // 추가 검증: 클럽이 실제로는 삭제되지 않고 활성화 상태가 false로 변경됐는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );
        assertThat(club.isState()).isFalse(); // 활성화 상태가 false인지 확인
    }

    @Test
    @DisplayName("클럽 정보 삭제 - 존재하지 않는 클럽")
    @WithUserDetails(value = "hgd222@test.com") //1번 유저로 로그인
    void deleteNonExistentClub() throws Exception {
        // given
        Long nonExistentClubId = 999L; // 존재하지 않는 클럽 ID

        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/" + nonExistentClubId)
                        .with(request -> {
                            request.setMethod("DELETE"); // DELETE 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("모임을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("클럽 정보 삭제 - 권한 없는 유저")
    @WithUserDetails(value = "lyh3@test.com") //3번 유저로 로그인
    void deleteClubWithoutPermission() throws Exception {
        // given
        // 클럽 생성
        Long clubId = 1L; // 테스트를 위해 클럽 ID를 1로 고정
        Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new IllegalStateException("클럽이 존재하지 않습니다."));


        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/" + club.getId())
                        .with(request -> {
                            request.setMethod("DELETE"); // DELETE 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("클럽 정보 조회")
    void getClubInfo() throws Exception {
        // given
        // 리더 생성
        MemberRegisterDto dto = new MemberRegisterDto(
                "testLeader@gmail.com",
                "12345678",
                "testLeader",
                "I'm a test leader"
        );
        memberService.registerMember(dto);

        Member member = memberService.findMemberByEmail(dto.email());

        // 클럽 생성
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
                        .imageUrl("https://example.com/image.jpg")
                        .isPublic(true)
                        .leaderId(member.getId())
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/" + club.getId())
                        .with(request -> {
                            request.setMethod("GET"); // GET 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("getClubInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 정보가 조회됐습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.name").value(club.getName()))
                .andExpect(jsonPath("$.data.bio").value(club.getBio()))
                .andExpect(jsonPath("$.data.category").value(club.getCategory().name()))
                .andExpect(jsonPath("$.data.mainSpot").value(club.getMainSpot()))
                .andExpect(jsonPath("$.data.maximumCapacity").value(club.getMaximumCapacity()))
                .andExpect(jsonPath("$.data.recruitingStatus").value(club.isRecruitingStatus()))
                .andExpect(jsonPath("$.data.eventType").value(club.getEventType().name()))
                .andExpect(jsonPath("$.data.startDate").value(club.getStartDate().toString()))
                .andExpect(jsonPath("$.data.endDate").value(club.getEndDate().toString()))
                .andExpect(jsonPath("$.data.isPublic").value(club.isPublic()))
                .andExpect(jsonPath("$.data.imageUrl").value(club.getImageUrl()))
                .andExpect(jsonPath("$.data.leaderId").value(club.getLeaderId()))
                .andExpect(jsonPath("$.data.leaderName").value(dto.nickname()));
    }

    @Test
    @DisplayName("클럽 정보 조회 - 존재하지 않는 클럽")
    void getNonExistentClubInfo() throws Exception {
        // given
        Long nonExistentClubId = 999L; // 존재하지 않는 클럽 ID

        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/" + nonExistentClubId)
                        .with(request -> {
                            request.setMethod("GET"); // GET 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 ID의 클럽을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("공개 클럽 목록 조회")
    void getPublicClubList() throws Exception {
        // given
        // testinitdata의 club 정보 이용
        Club club1 = clubService.getClubById(1L).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        Club club2 = clubService.getClubById(4L).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        // when
        ResultActions resultActions = mvc.perform(
                multipart("/api/v1/clubs/public")
                        .with(request -> {
                            request.setMethod("GET"); // GET 메소드로 요청
                            return request;
                        })
        ).andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("getPublicClubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("공개 클럽 목록이 조회됐습니다."))

                .andExpect(jsonPath("$.data.content.length()").value(2)) // 공개 클럽은 두개

                .andExpect(jsonPath("$.data.content[0].clubId").value(club1.getId()))
                .andExpect(jsonPath("$.data.content[0].name").value(club1.getName()))
                .andExpect(jsonPath("$.data.content[0].category").value(club1.getCategory().name()))
                .andExpect(jsonPath("$.data.content[0].imageUrl").value(club1.getImageUrl()))
                .andExpect(jsonPath("$.data.content[0].mainSpot").value(club1.getMainSpot()))
                .andExpect(jsonPath("$.data.content[0].eventType").value(club1.getEventType().name()))
                .andExpect(jsonPath("$.data.content[0].startDate").value(club1.getStartDate().toString()))
                .andExpect(jsonPath("$.data.content[0].endDate").value(club1.getEndDate().toString()))

                .andExpect(jsonPath("$.data.content[1].clubId").value(club2.getId()))
                .andExpect(jsonPath("$.data.content[1].name").value(club2.getName()))
                .andExpect(jsonPath("$.data.content[1].category").value(club2.getCategory().name()))
                .andExpect(jsonPath("$.data.content[1].imageUrl").value(club2.getImageUrl()))
                .andExpect(jsonPath("$.data.content[1].mainSpot").value(club2.getMainSpot()))
                .andExpect(jsonPath("$.data.content[1].eventType").value(club2.getEventType().name()))
                .andExpect(jsonPath("$.data.content[1].startDate").value(club2.getStartDate().toString()))
                .andExpect(jsonPath("$.data.content[1].endDate").value(club2.getEndDate().toString()));
    }


}