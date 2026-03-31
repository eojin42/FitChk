package com.sp.app.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// 클라이언트에 토큰을 보내기 위해 JwtToken DTO
@Builder
@Data
@AllArgsConstructor
public class JwtToken {
	// 토큰 정보
	private String grantType;
	private String accessToken;
	private Long accessTokenExpiresIn;
	private String refreshToken;
}
