package com.back.domain.preset;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.domain.preset.preset.repository.PresetRepository;
import com.back.global.enums.CheckListItemCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ApiV1PresetControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PresetRepository presetRepository;


  private Member member;
  private Preset preset;
  @BeforeEach
  void setUp() {
    member = memberRepository.findById(1L).orElseThrow(() -> new IllegalStateException("테스트용 멤버(ID: 1)가 존재하지 않습니다"));



    List<PresetItem> presetItems = new ArrayList<>();
    presetItems.add(PresetItem.builder()
        .content("테스트 아이템 내용 1")
        .category(CheckListItemCategory.RESERVATION)
        .sequence(1)
        .build());

    Preset presetBuilder = Preset.builder()
        .owner(member)
        .name("테스트 프리셋 1")
        .presetItems(presetItems)
        .build();

    preset = presetRepository.save(presetBuilder);
  }

  Long presetCreate() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    MvcResult result = mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    ).andReturn();
    String responseContent = result.getResponse().getContentAsString();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(responseContent);
    Long presetId = jsonNode.get("data").get("id").asLong();
    return presetId;
  }

  @Test
  @DisplayName("프리셋 생성 테스트")
  @WithUserDetails(value = "hgd222@test.com")
  void t1() throws Exception {

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.message").value("프리셋 생성 성공"))
        .andExpect(jsonPath("$.data.name").value("My Custom Preset"))
        .andExpect(jsonPath("$.data.presetItems[0].content").value("아이템 1"))
        .andExpect(jsonPath("$.data.presetItems[0].category").value(CheckListItemCategory.PREPARATION.name()))
        .andExpect(jsonPath("$.data.presetItems[1].content").value("아이템 2"))
        .andExpect(jsonPath("$.data.presetItems[1].category").value(CheckListItemCategory.ETC.name()))
        .andDo(print());

  }

  @Test
  @DisplayName("프리셋 생성 실패 - 멤버를 찾을 수 없음")
  @WithMockUser(username = "notfound@user.com")
  void t4() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("멤버를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - 잘못된 요청 형식")
  @WithUserDetails(value = "hgd222@test.com")
  void t5() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": ""
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("name-NotBlank-프리셋 이름은 필수입니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - 잘못된 카테고리")
  @WithUserDetails(value = "hgd222@test.com")
  void t6() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "INVALID_CATEGORY", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("요청 본문이 올바르지 않습니다."))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 세부 정보 조회")
  @WithUserDetails(value = "hgd222@test.com")
  void t7() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = presetCreate();

    // 생성된 프리셋의 ID를 사용하여 세부 정보를 조회합니다.
    mockMvc.perform(
        get("/api/v1/presets/" + presetId) // ID는 예시로 1을 사용합니다. 실제 테스트에서는 동적으로 가져와야 합니다.
            .contentType("application/json")
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 조회 성공"))
        .andExpect(jsonPath("$.data.name").value("My Custom Preset"))
        .andExpect(jsonPath("$.data.presetItems[0].content").value("아이템 1"))
        .andExpect(jsonPath("$.data.presetItems[0].category").value(CheckListItemCategory.PREPARATION.name()))
        .andExpect(jsonPath("$.data.presetItems[1].content").value("아이템 2"))
        .andExpect(jsonPath("$.data.presetItems[1].category").value(CheckListItemCategory.ETC.name()))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 세부 정보 조회 실패 - 프리셋이 존재하지 않음")
  @WithUserDetails(value = "hgd222@test.com")
  void t8() throws Exception {
    // 먼저 프리셋을 생성합니다.
    presetCreate();

    // 존재하지 않는 프리셋 ID로 조회 시도
    mockMvc.perform(
        get("/api/v1/presets/9999") // 존재하지 않는 ID
            .contentType("application/json")
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("프리셋을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 세부 정보 조회 실패 - 권한 없는 프리셋")
  @WithUserDetails(value = "chs4s@test.com")
  void t9() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = preset.getId();

    // 생성된 프리셋의 ID를 사용하여 세부 정보를 조회합니다.
    mockMvc.perform(
        get("/api/v1/presets/" + presetId) // ID는 예시로 1을 사용합니다. 실제 테스트에서는 동적으로 가져와야 합니다.
            .contentType("application/json")
    )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("권한 없는 프리셋"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 세부 정보 조회 실패 - 잘못된 요청 형식")
  @WithUserDetails(value = "hgd222@test.com")
  void t12() throws Exception {
    // 먼저 프리셋을 생성합니다.
    presetCreate();

    // 잘못된 요청 형식으로 조회 시도
    mockMvc.perform(
        get("/api/v1/presets/invalid-id") // 잘못된 ID 형식
            .contentType("application/json")
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("파라미터 'presetId'의 타입이 올바르지 않습니다. 요구되는 타입: Long"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 목록 조회")
  @WithUserDetails(value = "hgd222@test.com")
  void t13() throws Exception {
    long presetId1 = preset.getId();

    // 추가 프리셋 생성
    String requestBody2 = String.format("""
    {
      "presetItems": [
        { "content": "아이템 A", "category": "%s", "sequence":1 },
        { "content": "아이템 B", "category": "%s", "sequence":2 }
      ],
      "name": "테스트 프리셋 2"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody2)
    ).andExpect(status().isCreated());

    // 프리셋 목록 조회
    mockMvc.perform(
        get("/api/v1/presets")
            .contentType("application/json")
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 목록 조회 성공"))
        .andExpect(jsonPath("$.data.length()").value(2)) // 두 개의 프리셋이 있어야 함
        .andExpect(jsonPath("$.data[0].id").value(presetId1)) // 첫 번째 프리셋 ID 확인
        .andExpect(jsonPath("$.data[0].name").value("테스트 프리셋 1"))
        .andExpect(jsonPath("$.data[1].name").value("테스트 프리셋 2"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 목록 조회 - 빈 리스트 인 경우")
  @WithUserDetails(value = "hgd222@test.com")
  void t14() throws Exception {
    // 프리셋 목록 조회
    presetRepository.deleteAll();
    mockMvc.perform(
        get("/api/v1/presets")
            .contentType("application/json")
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 목록 조회 성공"))
        .andExpect(jsonPath("$.data.length()").value(0)) // 빈 리스트 확인
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 목록 조회 실패 - 멤버를 찾을 수 없음")
  @WithMockUser(username = "notfound@user.com")
  void t17() throws Exception {
    // 프리셋 목록 조회 시도
    mockMvc.perform(
        get("/api/v1/presets")
            .contentType("application/json")
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("멤버를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 삭제")
  @WithUserDetails(value = "hgd222@test.com")
  void t18() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = presetCreate();

    // 생성된 프리셋의 ID를 사용하여 삭제 요청을 보냅니다.
    mockMvc.perform(
        delete("/api/v1/presets/" + presetId)
            .contentType("application/json")
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 삭제 성공"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 삭제 실패 - 프리셋이 존재하지 않음")
  @WithUserDetails(value = "hgd222@test.com")
  void t19() throws Exception {
    // 존재하지 않는 프리셋 ID로 삭제 요청을 보냅니다.
    mockMvc.perform(
        delete("/api/v1/presets/9999") // 존재하지 않는 ID
            .contentType("application/json")
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("프리셋을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 삭제 실패 - 권한 없는 프리셋")
  @WithUserDetails(value = "chs4s@test.com")
  void t20() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = preset.getId();

    // 생성된 프리셋의 ID를 사용하여 삭제 요청을 보냅니다.
    mockMvc.perform(
        delete("/api/v1/presets/" + presetId)
            .contentType("application/json")
    )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("권한 없는 프리셋"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 삭제 실패 - 잘못된 요청 형식")
  @WithUserDetails(value = "hgd222@test.com")
  void t23() throws Exception {
    // 먼저 프리셋을 생성합니다.
    presetCreate();

    // 잘못된 요청 형식으로 삭제 시도
    mockMvc.perform(
        delete("/api/v1/presets/invalid-id") // 잘못된 ID 형식
            .contentType("application/json")
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("파라미터 'presetId'의 타입이 올바르지 않습니다. 요구되는 타입: Long"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정")
  @WithUserDetails(value = "hgd222@test.com")
  void t24() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = presetCreate();

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "수정된 아이템 1", "category": "%s", "sequence":1 },
        { "content": "수정된 아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "수정된 프리셋 이름"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        put("/api/v1/presets/" + presetId)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 수정 성공"))
        .andExpect(jsonPath("$.data.name").value("수정된 프리셋 이름"))
        .andExpect(jsonPath("$.data.presetItems[0].content").value("수정된 아이템 1"))
        .andExpect(jsonPath("$.data.presetItems[0].category").value(CheckListItemCategory.PREPARATION.name()))
        .andExpect(jsonPath("$.data.presetItems[1].content").value("수정된 아이템 2"))
        .andExpect(jsonPath("$.data.presetItems[1].category").value(CheckListItemCategory.ETC.name()))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정 실패 - 프리셋이 존재하지 않음")
  @WithUserDetails(value = "hgd222@test.com")
  void t25() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "수정된 아이템 1", "category": "%s", "sequence":1 },
        { "content": "수정된 아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "수정된 프리셋 이름"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        put("/api/v1/presets/9999") // 존재하지 않는 ID
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("프리셋을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정 실패 - 권한 없는 프리셋")
  @WithUserDetails(value = "chs4s@test.com")
  void t26() throws Exception {
    Long presetId = preset.getId();
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "수정된 아이템 1", "category": "%s", "sequence":1 },
        { "content": "수정된 아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "수정된 프리셋 이름"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        put("/api/v1/presets/" + presetId)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("권한 없는 프리셋"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정 실패 - 잘못된 요청 형식")
  @WithUserDetails(value = "hgd222@test.com")
  void t29() throws Exception {
    // 먼저 프리셋을 생성합니다.
    presetCreate();

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "수정된 아이템 1", "category": "%s", "sequence":1 },
        { "content": "수정된 아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": ""
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        put("/api/v1/presets/1") // ID는 예시로 1을 사용합니다. 실제 테스트에서는 동적으로 가져와야 합니다.
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("name-NotBlank-프리셋 이름은 필수입니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정 실패 - 잘못된 카테고리")
  @WithUserDetails(value = "hgd222@test.com")
  void t30() throws Exception {
    // 먼저 프리셋을 생성합니다.
    presetCreate();

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "수정된 아이템 1", "category": "INVALID_CATEGORY", "sequence":1 },
        { "content": "수정된 아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "수정된 프리셋 이름"
    }
    """, CheckListItemCategory.ETC.name());

    mockMvc.perform(
        put("/api/v1/presets/1") // ID는 예시로 1을 사용합니다. 실제 테스트에서는 동적으로 가져와야 합니다.
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("요청 본문이 올바르지 않습니다."))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 수정 성공 - 프리셋 아이템이 비어있을 경우")
  @WithUserDetails(value = "hgd222@test.com")
  void t31() throws Exception {
    // 먼저 프리셋을 생성합니다.
    long presetId = presetCreate();

    String requestBody = """
    {
      "presetItems": [],
      "name": "빈 아이템 프리셋"
    }
    """;

    mockMvc.perform(
        put("/api/v1/presets/" + presetId)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("프리셋 수정 성공"))
        .andExpect(jsonPath("$.data.name").value("빈 아이템 프리셋"))
        .andExpect(jsonPath("$.data.presetItems.length()").value(0)) // 빈 아이템 리스트 확인
        .andDo(print());
  }


}
