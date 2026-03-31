package com.sp.app.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sp.app.security.JwtAuthenticationFilter;
import com.sp.app.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
	// JWT 설정(API 용)
	@Bean
	@Order(1)
	SecurityFilterChain apiFilterChain(HttpSecurity http, JwtTokenProvider tokenProvider) throws Exception {
		http
			.securityMatcher("/api/**") // /api 로 시작하는 요청에만 적용
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable()) // API는 보통 CSRF 비활성화
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**", "/api/userIdCheck", "/api/signup").permitAll()
				.anyRequest().authenticated() // 인증된 사용자만 가능
			)
			.addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// Security 설정 우선순위가 WebMvcConfigurer 보다 높음
	// CORS 상세 설정 빈 등록
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 프론트엔드 주소 허용
		configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost")); 
		
		// 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		
		// 모든 헤더 허용
		configuration.setAllowedHeaders(List.of("*"));
		
		// 자격 증명(쿠키, 인증 헤더 등) 허용
		configuration.setAllowCredentials(true);
		
		// 브라우저가 캐싱할 시간 설정
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		
		return source;
	}
	
	// 세션 설정(일반 웹용)
	@Bean
	@Order(2)
	SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
		// ?continue 제거를 위해
		HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		requestCache.setMatchingRequestParameterName(null);		
		
		String[] excludeUri = { "/", "/member/login", "/member/logout",
				"/member/pwdFind", "/member/expired", "/dist/**",
				"/uploads/image/**", "/uploads/editor/**", 
				"/favicon.ico", "/WEB-INF/views/**"};
		
		http.cors(Customizer.withDefaults()) // CORS 설정 : 기본값 사용
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
			.requestCache(request -> request.requestCache(requestCache));		
		
		http.authorizeHttpRequests(auth -> auth
			.requestMatchers(excludeUri).permitAll()
			.requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMP")
			.anyRequest().authenticated()
		)
		.formLogin(form -> form
			.loginPage("/member/login")
			.loginProcessingUrl("/member/login")			
			.usernameParameter("login_id")
			.passwordParameter("password")			
			.defaultSuccessUrl("/admin")
			.failureUrl("/member/login?error")
		)
		.logout(logout-> logout
    		.logoutUrl("/member/logout")
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID")    		
    		.logoutSuccessUrl("/member/login")
    	)
		.sessionManagement(session -> session
			.sessionFixation().changeSessionId() // 세션 고정 보호
			.maximumSessions(1)
       		.expiredUrl("/member/expired")
		);

		http.exceptionHandling(exceptionConfig -> exceptionConfig
			.accessDeniedPage("/member/noAuthorized")
		);
		
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
