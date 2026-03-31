package com.sp.app.service;

import java.util.Objects;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.LoginUser;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.RefreshTokenDto;
import com.sp.app.domain.dto.TokenRequestDto;
import com.sp.app.mapper.MemberMapper;
import com.sp.app.security.JwtToken;
import com.sp.app.security.JwtTokenProvider;
import com.sp.app.security.NumericRoleGranted;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
	private final MemberMapper mapper;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	@Override
	public JwtToken login(String username, String password) throws Exception {
		try {
			// 1. username + password 를 기반으로 Authentication 객체 생성
			//    authentication 은 인증 여부를 확인하는 authenticated 값이 false
			UsernamePasswordAuthenticationToken authenticationToken = 
					new UsernamePasswordAuthenticationToken(username, password);
			
			// 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
			//    authenticate 메소드가 실행될 때 CustomUserDetailsService 의 loadUserByUsername 메소드 실행
			Authentication authentication = authenticationManagerBuilder.getObject()
					.authenticate(authenticationToken);

			// 3. 인증 정보를 기반으로 JWT 토큰 생성
			JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

			// 4. RefreshToken 저장
			RefreshTokenDto refreshToken = RefreshTokenDto.builder()
					.login_id(authentication.getName())
					.rt_value(jwtToken.getRefreshToken())
					.build();

			RefreshTokenDto dto = mapper.findByToken(username);
			if(dto == null) {
				mapper.insertRefreshToken(refreshToken);
			} else {
				mapper.updateRefreshToken(refreshToken);
			}

			// 5. 토큰발급
			return jwtToken;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("토큰 발급중 에러 발생 : ", e);
		}
	}

	@Transactional
	@Override
	public JwtToken reissue(TokenRequestDto tokenRequestDto) throws Exception {
		// 토큰 재발급
		
		try {
			// 1. Refresh Token 검증
	        if (! jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
	            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
	        }

	        // 2. Access Token 에서 로그인 정보 가져오기
	        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

	        // 3. 저장소에서 아이디를 기반으로 Refresh Token 값 가져옴
	        RefreshTokenDto refreshToken =Objects.requireNonNull(mapper.findByToken(authentication.getName()));

	        // 4. Refresh Token 일치하는지 검사
	        if (!refreshToken.getRt_value().equals(tokenRequestDto.getRefreshToken())) {
	            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
	        }

	        // 5. 새로운 토큰 생성
	        JwtToken tokenDto = jwtTokenProvider.generateToken(authentication);

	        // 6. 저장소 정보 업데이트
	        refreshToken.setRt_value(tokenDto.getRefreshToken());
	        mapper.updateRefreshToken(refreshToken);

	        // 토큰 발급
	        return tokenDto;

		} catch (NullPointerException e) {
			throw new RuntimeException("로그아웃 사용자 : ", e);
		} catch (Exception e) {
			throw new Exception("토큰 재발급중 에러 발생 : ", e);
		}
	}

	@Override
	public MemberDto findById(String login_id) {
		MemberDto dto = null;

		try {
			dto = Objects.requireNonNull(mapper.findByLoginId(login_id));
		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return dto;
	}
	
	@Override
	public String findByAuthority(String login_id) {
		String authority = null;
		
		try {
			authority = mapper.findByAuthority(login_id);
		} catch (Exception e) {
		}
		
		return authority;
	}
	
	@Override
	public LoginUser getLoginUser(String accessToken) {
		try {
			if(accessToken == null || accessToken.isEmpty()) {
				return null;
			}
			
			Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
			
			MemberDto dto = mapper.findByLoginId(authentication.getName());
			String authority = mapper.findByAuthority(authentication.getName());
			
			LoginUser loginUser = LoginUser.builder()
					.member_id(dto.getMember_id())
					.login_id(dto.getLogin_id())
					.name(dto.getName())
					.email(dto.getEmail())
					.avatar(dto.getProfile_photo())
					.role(authority)
					.userLevel(NumericRoleGranted.getUserLevel(authority))
					.build();
			return loginUser;
		} catch (Exception e) {
		}
		
		return null;
	}

}
