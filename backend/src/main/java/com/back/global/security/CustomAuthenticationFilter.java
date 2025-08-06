package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final MemberService memberService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Processing request for " + request.getRequestURI());

        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.code());
            response.getWriter().write(
                    Ut.json.toString(rsData)
            );
        } catch (Exception e) {
            throw e;
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // API 요청이 아니라면 패스
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증, 인가가 필요없는 API 요청이라면 패스
        if (List.of(
                "/api/v1/members/auth/login",
                "/api/v1/members/auth/register",
                "/api/v1/members/auth/guest-register",
                "/api/v1/members/auth/guest-login",
                "/api/v1/clubs/public"
        ).contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken;

        // 액세스 토큰을 헤더나 쿠키에서 가져오기
        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ") || headerAuthorization.length() <= 7)
                throw new ServiceException(401, "Authorization 헤더가 Bearer 형식이 아닙니다.");

            accessToken = headerAuthorization.substring(7);

        } else {
            accessToken = rq.getCookieValue("accessToken", "");
        }

        logger.debug("accessToken : " + accessToken);

        boolean isAccessTokenExists = !accessToken.isBlank();

        if (!isAccessTokenExists) {
            filterChain.doFilter(request, response);
            return;
        }


        Member member = null;
        boolean isAccessTokenValid = false;

        // accessToken이 존재하는 경우, 해당 토큰의 유효성을 검사
        if (isAccessTokenExists){
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                Object emailObj = payload.get("email");

                String email = null;

                if (emailObj instanceof String) {
                    email = (String) emailObj;
                }

                if (email != null) {
                    Member DbMember = memberService.findMemberByEmail(email);

                    if (DbMember != null) {
                        member = DbMember;
                        isAccessTokenValid = true;
                    }
                }
            }
        }


        // Access Token이 유효하지 않은 경우 에러 처리
        if (!isAccessTokenValid) {
            throw new ServiceException(499, "access token이 유효하지 않습니다.");
        }


         //Access Token이 유효한 경우, 인증된 사용자로 설정
        MemberInfo memberInfo = member.getMemberInfo();

        UserDetails user = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getMemberType(),
                member.getPassword(),
                Collections.emptyList()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청이다.
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
