// com.back.domain.club.club.repository 아래에 ClubSpecification.java 생성
package com.back.domain.club.club.repository;

import com.back.domain.club.club.entity.Club;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ClubSpecification {

    // 이름(name)으로 부분 일치 검색
    public static Specification<Club> likeName(String name) {
        // name 파라미터가 비어있지 않은 경우에만 Specification을 반환
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    // 지역(mainSpot)으로 부분 일치 검색
    public static Specification<Club> likeMainSpot(String mainSpot) {
        if (!StringUtils.hasText(mainSpot)) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("mainSpot"), "%" + mainSpot + "%");
    }

    // 카테고리(category)로 완전 일치 검색
    public static Specification<Club> equalCategory(ClubCategory category) {
        // category가 null이 아닌 경우에만 Specification을 반환
        if (category == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    // 모집 유형(eventType)으로 완전 일치 검색
    public static Specification<Club> equalEventType(EventType eventType) {
        if (eventType == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("eventType"), eventType);
    }

    // 공개된 클럽만 조회하는 기본 조건
    public static Specification<Club> isPublic() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isPublic"));
    }
}