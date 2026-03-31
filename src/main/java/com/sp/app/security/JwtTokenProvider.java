package com.sp.app.security;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/*
  - Spring Security와 JWT 토큰을 사용하여 인증과 권한 부여를 처리하는 클래스
  - JWT 토큰의 생성, 복호화, 검증 기능을 구현
*/
@Slf4j
@Component
public class JwtTokenProvider {
	// Base64 인코딩된 Secret 키(최소 256비트 이상)
	private final String SECRET = "bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5";

	private static final String AUTHORITIES_KEY = "role";
	private static final String BEARER_TYPE = "Bearer";

	// private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30 분
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

	private final SecretKey secretKey;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	public JwtTokenProvider() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * 회원 정보를 이용하여 AccessToken, RefreshToken을 생성
	 * 
	 * @param authentication
	 * @return
	 */
	public JwtToken generateToken(Authentication authentication) {
		// 권한 가져오기
		String authorities = authentication.getAuthorities()
				.stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = (new Date()).getTime();

		// Access Token(인증된 사용자의 권한 정보와 만료 시간을 담고 있음) 생성
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
		String accessToken = Jwts.builder()
				.subject(authentication.getName())
				.claim(AUTHORITIES_KEY, authorities)
				.expiration(accessTokenExpiresIn)
				.signWith(secretKey)
				.compact();

		// Refresh Token(Access Token의 갱신을 위해 사용) 생성
		String refreshToken = Jwts.builder()
				.expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
				.signWith(secretKey)
				.compact();

		return JwtToken.builder()
				.grantType(BEARER_TYPE)
				.accessToken(accessToken)
				.accessTokenExpiresIn(accessTokenExpiresIn.getTime())
				.refreshToken(refreshToken)
				.build();
	}

	/**
	 * JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메소드
	 * 
	 * @param accessToken
	 * @return
	 * 
	 *   - 주어진 Access token을 복호화하여 사용자의 인증 정보(Authentication)를 생성
	 *   - 토큰의 Claims에서 권한 정보를 추출하고, User 객체를 생성하여 Authentication 객체로 반환
	 */
	public Authentication getAuthentication(String accessToken) {
		// Jwt 토큰 복호화
		Claims claims = parseClaims(accessToken);

		if (claims.get(AUTHORITIES_KEY) == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}
		
		String username = claims.getSubject();
	    UserDetails userDetails =
	            customUserDetailsService.loadUserByUsername(username);
		
	    return new UsernamePasswordAuthenticationToken(
	    		userDetails,
	    		null,
	    		userDetails.getAuthorities()
	    );
	}

	/**
	 * JWT 유효성 검증
	 * 
	 * @param token
	 * @return
	 * 
	 *  - Jwts.parserBuilder를 사용하여 토큰의 서명 키를 설정하고, 예외 처리를 통해 토큰의 유효성 여부를 판단
	 *  - claim.getSubject()는 주어진 토큰의 클레임에서 "sub" 클레임의 값을 반환
	 *     : 토큰의 주체를 나타냄.
	 *     : 예) 사용자의 식별자나 이메일 주소
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.info("Invalid JWT Token", e);
			throw new JwtException("유효하지 않은 토큰입니다.");
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT Token", e);
			throw e;
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT Token", e);
			throw new JwtException("지원되지 않는 토큰입니다.");
		} catch (IllegalArgumentException e) {
			// 토큰이 올바른 형식이 아니거나 클레임이 비어있는 경우 등에 발생
			log.info("JWT claims string is empty.", e);
			throw new JwtException("토큰이 비어있습니다.");
		}
	}

	/**
	 * 만료 여부 상관없이 모든 정보 추출
	 * 
	 * @param accessToken
	 * @return
	 * 
	 *    ## accessToken
	 *       - 클레임(Claims) : 토큰에서 사용할 정보의 조각
	 *       - 주어진 Access token을 복호화하고, 만료된 토큰인 경우에도 Claims 반환
	 *       - parseClaimsJws() 메소드가 JWT 토큰의 검증과 파싱을 모두 수행
	 */
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(accessToken)
					.getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
