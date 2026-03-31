package com.sp.app.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.LoginUser;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;

/*
 ## UserDetailsService
   - 스프링 시큐리티에서 사용자 인증을 처리할 때 사용되는 인터페이스
   - 주로 사용자 정보를 데이터베이스나 다른 저장소에서 조회하여 인증 및 권한 부여에 필요한 사용자 정보를 제공하는 역할
   - 스프링 시큐리티는 UserDetailsService를 통해 사용자가 로그인할 때 필요한 
     사용자 정보(Username, Password, 권한 등)을 UserDetails 객체로 반환하여 인증 처리 및 권한 검증을 진행
*/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberService memberService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberDto member = memberService.findById(username);
		
		if (member == null) {
			throw new UsernameNotFoundException("아이디가 존재하지 않습니다.");
		}

		List<String> authorities = new ArrayList<>();
		String authority = memberService.findByAuthority(username);
		authorities.add(authority);

		return toUserDetails(member, authorities);
	}

    private UserDetails toUserDetails(MemberDto member, List<String> authorities) {
    	LoginUser loginUser = LoginUser.builder()
				.member_id(member.getMember_id())
				.login_id(member.getLogin_id())
				.password(member.getPassword())
				.name(member.getName())
				.email(member.getEmail())
				.userLevel(NumericRoleGranted.getUserLevel(member.getAuthority()))
				.avatar(member.getProfile_photo())
				.login_type("local")
				.build();

		return CustomUserDetails.builder()
				.loginUser(loginUser)
				.disabled(member.getEnabled() == 0)
				.roles(authorities)
				.build();
    }
}
