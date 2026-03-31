package com.sp.app.service;

import com.sp.app.domain.dto.LoginUser;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.TokenRequestDto;
import com.sp.app.security.JwtToken;

public interface AuthService {
	public JwtToken login(String username, String password) throws Exception;
	public JwtToken reissue(TokenRequestDto tokenRequestDto) throws Exception;
	public MemberDto findById(String login_id);
	public String findByAuthority(String login_id);
	
	public LoginUser getLoginUser(String accessToken);
}
