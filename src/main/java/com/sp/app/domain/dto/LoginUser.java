package com.sp.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 세션에 저장할 정보(아이디, 이름, 역할(권한) 등)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUser {
	private long member_id;
	private String login_id;
	private String password;
	private String name;
	private String role;
	private String email;
	private int userLevel;
	private String login_type; // local, kakao, naver, google
	private String avatar; // profile photo
	private String grantType;
}
