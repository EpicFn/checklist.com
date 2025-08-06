package com.back.domain.auth.service;

import com.back.domain.member.member.entity.Member;
import com.back.global.config.jwt.JwtProperties;
import com.back.global.enums.MemberType;
import com.back.standard.util.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final JwtProperties jwtProperties;

    public AuthService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Member member) {
        //1. 회원, 비회원 공통 검증
        if (member == null) {
            throw new IllegalArgumentException("Member 정보가 없습니다.");
        }

        long id = member.getId();
        String email = "";
        String nickname = member.getNickname();
        String tag = member.getTag();
        MemberType memberType = member.getMemberType();

        //2. 회원 검증
        if (member.getMemberInfo() != null) {
            email = member.getMemberInfo().getEmail();
        }

        return Ut.jwt.toString(
                jwtProperties.getJwt().getSecretKey(),
                (int) jwtProperties.getExpirationSecondsAsLong(),
                Map.of(
                        "id", id,
                        "email", email == null ? "" : email,
                        "nickname", nickname,
                        "tag", tag,
                        "memberType", memberType.toString()
                )
        );
    }

    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtProperties.getJwt().getSecretKey(), accessToken);

        if (parsedPayload == null) return null;

        String email = (String) parsedPayload.get("email");

        return Map.of("email", email);
    }
}