package com.example.cinema.config.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ProjectSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    @Bean
    @Order(0)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(toH2Console())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        /* ==================================================
                         * PUBLIC (인증 불필요)
                         * ================================================== */
                        //Socket Connection
                        .requestMatchers(
                                "/ws/**",
                                "/ws",
                                "/error"
                        ).permitAll()
                        // 인증
                        .requestMatchers(
                                "/auth/login",
                                "/auth/signup",
                                "/auth/reissue",
                                "/error"
                        ).permitAll()

                        // 검색 / 조회
                        .requestMatchers(HttpMethod.GET,
                                "/users/search/**",
                                "/users/*/contents",
                                "/contents/search",
                                "/contents",
                                "/media-assets/**",
                                "/schedules/**"
                        ).permitAll()

                        // 테스트 페이지
                        .requestMatchers(
                                "/subscription-test",
                                "/subscription-test.html",
                                "/test/**",
                                "/api/test/**",
                                "/ws-sockjs/**"
                        ).permitAll()

                        /* ==================================================
                         * PROTECTED (인증 필요)
                         * ================================================== */

                        // 인증
                        .requestMatchers(
                                "/auth/logout"
                        ).authenticated()

                        // 사용자
                        .requestMatchers(
                                "/api/v1/users",
                                "/users/me"
                        ).authenticated()

                        // 콘텐츠 관리
                        .requestMatchers(
                                "/contents/*/edit",
                                "/contents/*/encoding-status",
                                "/contents",
                                "/contents/**"
                        ).authenticated()

                        // 미디어 에셋 (업로드 / 메타데이터)
                        .requestMatchers(
                                "/media-assets/**"
                        ).authenticated()

                        // 인프라 (Presigned URL, 매핑)
                        .requestMatchers(
                                "/api/contents/assets/**",
                                "/api/assets/**"
                        ).authenticated()

                        // 구독
                        .requestMatchers(
                                "/users/subscription",
                                "/users/subscriptions",
                                "/users/subscriptions/**"
                        ).authenticated()

                        // 정산
                        .requestMatchers(
                                "/settlements/**"
                        ).authenticated()

                        // 플렛폼 수입 조회
                        .requestMatchers(
                                "/admin/platform-revenue/**"
                        ).permitAll()

                        // 상영 스케줄 / 상영관
                        .requestMatchers(
                                "/schedules/**",
                                "/theaters/**"
                        ).authenticated()

                        // 리뷰
                        .requestMatchers(
                                "/contents/reviews"
                        ).authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
