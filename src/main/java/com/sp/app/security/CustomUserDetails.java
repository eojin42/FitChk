package com.sp.app.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sp.app.domain.dto.LoginUser;

// UserDetails : 사용자 인증(Authentication) 정보를 담기 위한 핵심 인터페이스
// 커스텀 UserDetails
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private final LoginUser memebr;
	private final List<String> roles; // 여러 권한 처리를 위해 리스트로 관리
	private final boolean disabled;
	
	private CustomUserDetails(Builder builder) {
		this.memebr = builder.memebr;
		this.roles = builder.roles;
		this.disabled = builder.disabled;
	}

	// Builder 클래스 정의
	public static class Builder {
		private LoginUser memebr;
		private List<String> roles;
		private boolean disabled = false;
        
		public Builder loginUser(LoginUser memebr) {
			this.memebr = memebr;
			return this;
		}

		public Builder roles(List<String> roles) {
			this.roles = roles;
			return this;
		}
        
		public Builder disabled(boolean disabled) {
			this.disabled = disabled;
			return this;
		}        

		public CustomUserDetails build() {
			if (this.memebr == null) {
				throw new IllegalStateException("SessionInfo 객체는 필수입니다.");
			}
        	
			return new CustomUserDetails(this);
		 }
	}

	// 빌더 시작 정적 메서드
	public static Builder builder() {
		return new Builder();
	}

	// UserDetails 필수 구현 메서드
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream()
			.map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
			.collect(Collectors.toList());
	}

	@Override
	public String getPassword() { return memebr.getPassword(); }

	@Override
	public String getUsername() { return memebr.getLogin_id(); }

	@Override 
	public boolean isEnabled() { return !disabled; }
    
	// 나머지 설정 디폴트
	@Override public boolean isAccountNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }

	public LoginUser getLoginUser() { return memebr; }
}
