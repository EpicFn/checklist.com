package com.back.global.security;

import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Optional;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomAuthenticationFilter customAuthenticationFilter;
  private final Optional<MockAuthFilterForSpecificApi> mockAuthFilterForSpecificApi;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
        .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/clubs/invitations/**").permitAll()
            .requestMatchers("/favicon.ico").permitAll() // 파비콘 접근 허용 (검색 엔진 최적화)
            .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 접근 허용
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/swagger-resources",
                "/webjars/**"
            ).permitAll()
            .requestMatchers(
                "/api/v1/members/auth/register",
                "/api/v1/members/auth/login",
                "/api/v1/members/auth/guest-register",
                "/api/v1/members/auth/guest-login"
            ).permitAll() // 회원가입, 로그인 허용
            .requestMatchers(
                    "/api/v1/clubs/{clubId:[0-9]+}",
                "/api/v1/clubs/public"
            ).permitAll() // 클럽 정보 조회 및 공개 클럽 목록 접근 허용
            .requestMatchers(HttpMethod.POST, "/api/v1/clubs/invitations/{token}/apply").authenticated()
            .anyRequest().authenticated() // 나머지 요청은 인증 필요
        )
        .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (API 서버에서는 일반적으로 비활성화)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(AbstractHttpConfigurer::disable)
        .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .headers(
            headers -> headers
                .frameOptions(
                    HeadersConfigurer.FrameOptionsConfig::sameOrigin
                )
        )
        .exceptionHandling(
            exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setContentType("application/json;charset=UTF-8");

                      response.setStatus(401);
                      response.getWriter().write(
                          Ut.json.toString(
                              RsData.of(
                                  401,
                                  "로그인 후 이용해주세요."
                              )

                          )
                      );
                    }
                )
                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                      response.setContentType("application/json;charset=UTF-8");

                      response.setStatus(403);
                      response.getWriter().write(
                          Ut.json.toString(
                              RsData.of(
                                  403,
                                  "권한이 없습니다."
                              )
                          )
                      );
                    }
                )
        );

    // Profile test 일때 Mock 인증 필터를 특정 API에만 적용
    mockAuthFilterForSpecificApi.ifPresent(filter ->
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
    );

    return http.build();
  }

  // CORS 설정을 위한 Bean 추가
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 허용할 도메인 주소들 (필요에 따라 수정하세요)
    configuration.addAllowedOrigin("http://localhost:3000");  // React 개발서버
    configuration.addAllowedOrigin("http://localhost:8080");  // 다른 포트
    configuration.addAllowedOrigin("http://127.0.0.1:3000");  // 127.0.0.1
//     configuration.addAllowedOrigin("https://yourdomain.com"); // 실제 배포 도메인

    // 허용할 HTTP 메소드들
    configuration.addAllowedMethod("GET");
    configuration.addAllowedMethod("POST");
    configuration.addAllowedMethod("PUT");
    configuration.addAllowedMethod("DELETE");
    configuration.addAllowedMethod("OPTIONS");
    configuration.addAllowedMethod("PATCH");

    // 허용할 헤더들
    configuration.addAllowedHeader("*");

    // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration); // API 경로만 CORS 허용
    return source;
  }
}