package com.back.global.security;

import com.back.global.enums.MemberType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile("test") // 테스트 프로파일에서만 활성화
@Order(1)
public class MockAuthFilterForSpecificApi extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 특정 API 경로에만 필터 적용
        return !request.getRequestURI().startsWith("/api/v1/schedules");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증 정보가 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // 테스트용 SecurityUser 생성
            SecurityUser testUser = new SecurityUser(
                    1L,
                    "홍길동",
                    "fakeTag",
                    MemberType.MEMBER,
                    "password1",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    testUser,
                    null,
                    testUser.getAuthorities()
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}