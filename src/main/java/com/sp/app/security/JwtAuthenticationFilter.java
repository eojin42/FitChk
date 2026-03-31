package com.sp.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
## OncePerRequestFilter
  1) 개요
     - Spring Framework에서 제공하는 필터 클래스 중 하나
     - HTTP 요청마다 한 번만 실행되는 필터를 만들 때 사용
     - 주로 Spring Security에서 JWT 인증, 로깅, CORS 처리 등에 자주 사용
  2) 특징
    - 한 요청당 한 번 실행
      필터 체인 내에서 같은 요청이 여러 번 들어와도 중복 실행되지 않음을 보장
    - 추상 클래스
      직접 구현할 때는 doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 메서드를 오버라이드
    - Spring Security와 잘 어울림
      인증, 권한 체크, JWT 토큰 검증 등을 각 요청마다 한번만 처리하고 싶을 때 사용
*/
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	/*
	  - resolveToken() 메서드를 사용하여 요청 헤더에서 JWT 토큰을 추출
	  - JwtTokenProvider의 validateToken() 메서드로 JWT 토큰의 유효성 검증
	  - 토큰이 유효하면 JwtTokenProvider의 getAuthentication() 메소드로 인증 객체 가져와서 SecurityContext에 저장
	    : 요청을 처리하는 동안 인증 정보가 유지된다
	  - chain.doFilter()를 호출하여 다음 필터로 요청을 전달 
	*/
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		try {
			// request Header에서 JWT 토큰 추출
			String token = resolveToken((HttpServletRequest) request);
			
			// validateToken으로 토큰 유효성 검사
			if (token != null && jwtTokenProvider.validateToken(token)) {
				// 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			
			// 정상적인 경우 다음 필터로 이동
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			// 토큰 만료 시 401 에러 응답
	        setErrorResponse(response, "토큰이 만료되었습니다. 다시 로그인해주세요.");
		} catch (JwtException | IllegalArgumentException e) {
	        // 그 외 유효하지 않은 토큰일 때 401 에러 응답
	        setErrorResponse(response, "유효하지 않은 인증 토큰입니다.");
	    }

	}

	// 응답을 직접 작성하는 헬퍼 메서드
	private void setErrorResponse(HttpServletResponse resp, String message) throws IOException {
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resp.setContentType("application/json;charset=UTF-8");
		// JSON 형태로 에러 메시지 전달
		resp.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
	}
	
	/*
	  * Request Header에서 토큰 정보 추출
	    - 주어진 HttpServletRequest에서 토큰 정보를 추출하는 역할
	    - "Authorization" 헤더에서 "Bearer " 접두사로 시작하는 토큰을 추출하여 반환
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.split(" ")[1].trim();
		}

		return null;
	}
	
	
}
