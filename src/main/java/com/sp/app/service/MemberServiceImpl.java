package com.sp.app.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.mail.Mail;
import com.sp.app.mail.MailSender;
import com.sp.app.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
	private final MemberMapper mapper;
	private final StorageService storageService;
	private final MailSender mailSender;
	private final PasswordEncoder bcryptEncoder;
	
	@Override
	public MemberDto loginSnsMember(Map<String, Object> map) {
		MemberDto dto = null;

		try {
			dto = mapper.loginSnsMember(map);
		} catch (Exception e) {

			log.info("loginSnsMember : ", e);
		}

		return dto;
	}

	@Transactional(rollbackFor = { Exception.class })
	@Override
	public void insertMember(MemberDto dto, String uploadPath) throws Exception {
		try {
			if (!dto.getSelectFile().isEmpty()) {
				String saveFilename = storageService.uploadFileToServer(dto.getSelectFile(), uploadPath);
				dto.setProfile_photo(saveFilename);
			}

			// 회원정보 저장

			// 패스워드 암호화
			String encPassword = bcryptEncoder.encode(dto.getPassword());
			dto.setPassword(encPassword);

			/*
			 * Long seq = mapper.memberSeq(); dto.setMember_id(seq);
			 * mapper.insertMember1(dto); mapper.insertMember2(dto);
			 */
			mapper.insertMember12(dto); // member1, member2 테이블 동시에

			// 권한저장
			dto.setAuthority("USER");
			mapper.insertAuthority(dto);

		} catch (Exception e) {
			log.info("insertMember : ", e);

			throw e;
		}
	}

	@Transactional(rollbackFor = { Exception.class })
	@Override
	public void insertSnsMember(MemberDto dto) throws Exception {
		try {
			Long seq = mapper.memberSeq();
			dto.setMember_id(seq);

			mapper.insertSnsMember(dto);
		} catch (Exception e) {
			log.info("insertSnsMember : ", e);
			throw e;
		}
	}

	@Override
	public void insertMemberStatus(MemberDto dto) throws Exception {
		try {
			mapper.insertMemberStatus(dto);
		} catch (Exception e) {
			log.info("updateLastLogin : ", e);

			throw e;
		}
	}

	@Override
	public void updatePassword(MemberDto dto) throws Exception {
		if (isPasswordCheck(dto.getLogin_id(), dto.getPassword())) {
			throw new RuntimeException("패스워드가 기존 패스워드와 일치합니다.");
		}

		try {
			String encPassword = bcryptEncoder.encode(dto.getPassword());
			dto.setPassword(encPassword);

			mapper.updateMemberPassword(dto);
		} catch (Exception e) {
			log.info("updatePassword : ", e);

			throw e;
		}
	}

	@Override
	public void updateMemberEnabled(Map<String, Object> map) throws Exception {
		try {
			mapper.updateMemberEnabled(map);
		} catch (Exception e) {
			log.info("updateMemberEnabled : ", e);

			throw e;
		}
	}

	@Transactional(rollbackFor = { Exception.class })
	@Override
	public void updateMember(MemberDto dto, String uploadPath) throws Exception {
		try {
			// 업로드한 파일이 존재한 경우
			if (dto.getSelectFile() != null && !dto.getSelectFile().isEmpty()) {
				if (!dto.getProfile_photo().isBlank()) {
					storageService.deleteFile(uploadPath, dto.getProfile_photo());
				}

				String saveFilename = storageService.uploadFileToServer(dto.getSelectFile(), uploadPath);
				dto.setProfile_photo(saveFilename);
			}

			boolean bPwdUpdate = !isPasswordCheck(dto.getLogin_id(), dto.getPassword());
			if (bPwdUpdate) {
				// 패스워드가 변경된 경우만 member1 테이블의 패스워드 변경
				String encPassword = bcryptEncoder.encode(dto.getPassword());
				dto.setPassword(encPassword);

				mapper.updateMemberPassword(dto);
			}
			mapper.updateMember2(dto);

		} catch (Exception e) {
			log.info("updateMember : ", e);

			throw e;
		}
	}

	@Override
	public void updateLastLogin(Long member_id) throws Exception {
		try {
			mapper.updateLastLogin(member_id);
		} catch (Exception e) {
			log.info("updateLastLogin : ", e);

			throw e;
		}
	}

	@Override
	public void updateLastLogin(String login_id) throws Exception {
		try {
			mapper.updateLastLoginId(login_id);
		} catch (Exception e) {
			log.info("updateLastLoginId : ", e);

			throw e;
		}
	}

	@Override
	public MemberDto findById(Long member_id) {
		MemberDto dto = null;

		try {
			dto = Objects.requireNonNull(mapper.findById(member_id));
		} catch (NullPointerException e) {
		} catch (ArrayIndexOutOfBoundsException e) {
		} catch (Exception e) {
			log.info("findById : ", e);
		}

		return dto;
	}

	@Override
	public MemberDto findById(String login_id) {
		MemberDto dto = null;

		try {
			dto = Objects.requireNonNull(mapper.findByLoginId(login_id));
		} catch (NullPointerException e) {
		} catch (Exception e) {
			log.info("findById : ", e);
		}

		return dto;
	}

	@Override
	public Long getMemberId(String login_id) {
		try {
			Long result = Objects.requireNonNull(mapper.getMemberId(login_id));
			return result;
		} catch (Exception e) {
			log.info("getMemberId : ", e);
		}

		return 0L;
	}

	@Override
	public int checkFailureCount(String login_id) {
		int result = 0;

		try {
			result = mapper.checkFailureCount(login_id);
		} catch (Exception e) {
			log.info("checkFailureCount : ", e);
		}

		return result;
	}

	@Override
	public void updateFailureCountReset(String login_id) throws Exception {
		try {
			mapper.updateFailureCountReset(login_id);
		} catch (Exception e) {
			log.info("updateFailureCountReset : ", e);

			throw e;
		}
	}

	@Override
	public void updateFailureCount(String login_id) throws Exception {
		try {
			mapper.updateFailureCount(login_id);
		} catch (Exception e) {
			log.info("updateFailureCount : ", e);

			throw e;
		}
	}

	@Transactional(rollbackFor = { Exception.class })
	@Override
	public void deleteMember(Map<String, Object> map, String uploadPath) throws Exception {
		try {
			mapper.deleteAuthority(map);

			map.put("enabled", 0);
			mapper.updateMemberEnabled(map);

			String filename = (String) map.get("filename");
			if (filename != null && !filename.isBlank()) {
				storageService.deleteFile(uploadPath, filename);
			}

			mapper.deleteMember2(map);
			// mapper.deleteMember1(map);
		} catch (Exception e) {
			log.info("deleteMember : ", e);

			throw e;
		}
	}

	@Override
	public void deleteProfilePhoto(Map<String, Object> map, String uploadPath) throws Exception {
		try {
			String filename = (String) map.get("filename");
			if (filename != null && !filename.isBlank()) {
				storageService.deleteFile(uploadPath, filename);
			}

			mapper.deleteProfilePhoto(map);
		} catch (Exception e) {
			log.info("deleteProfilePhoto : ", e);

			throw e;
		}
	}

	@Override
	public void generatePwd(MemberDto dto) throws Exception {
		// 10 자리 임시 패스워드 생성

		String lowercase = "abcdefghijklmnopqrstuvwxyz";
		String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String digits = "0123456789";
		String special_characters = "!#@$%^&*()-_=+[]{}?";
		String all_characters = lowercase + digits + uppercase + special_characters;

		try {
			// 암호화적으로 안전한 난수 생성(예측 불가 난수 생성)
			SecureRandom random = new SecureRandom();

			StringBuilder sb = new StringBuilder();

			// 각 문자는 최소 하나 이상 포함
			sb.append(lowercase.charAt(random.nextInt(lowercase.length())));
			sb.append(uppercase.charAt(random.nextInt(uppercase.length())));
			sb.append(digits.charAt(random.nextInt(digits.length())));
			sb.append(special_characters.charAt(random.nextInt(special_characters.length())));

			for (int i = sb.length(); i < 10; i++) {
				int index = random.nextInt(all_characters.length());

				sb.append(all_characters.charAt(index));
			}

			// 문자 섞기
			StringBuilder password = new StringBuilder();
			while (sb.length() > 0) {
				int index = random.nextInt(sb.length());
				password.append(sb.charAt(index));
				sb.deleteCharAt(index);
			}

			String result;
			result = dto.getName() + "님의 새로 발급된 임시 패스워드는 <b> " + password.toString() + " </b> 입니다.<br>"
					+ "로그인 후 반드시 패스워드를 변경하시기 바랍니다.";

			Mail mail = new Mail();
			mail.setReceiverEmail(dto.getEmail());

			mail.setSenderEmail("메일설정이메일@도메인");
			mail.setSenderName("관리자");
			mail.setSubject("임시 패스워드 발급");
			mail.setContent(result);

			// 테이블의 패스워드 변경
			String encPassword = bcryptEncoder.encode(password.toString());
			dto.setPassword(encPassword);
			mapper.updateMemberPassword(dto);

			mapper.updateFailureCountReset(dto.getLogin_id());

			// 메일 전송
			boolean b = mailSender.mailSend(mail);

			if (!b) {
				throw new Exception("이메일 전송중 오류가 발생했습니다.");
			}

		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public List<MemberDto> listFindMember(Map<String, Object> map) {
		List<MemberDto> list = null;

		try {
			list = mapper.listFindMember(map);
		} catch (Exception e) {
			log.info("listFindMember : ", e);
		}

		return list;
	}

	@Override
	public String findByAuthority(String login_id) {
		String authority = null;

		try {
			authority = mapper.findByAuthority(login_id);
		} catch (Exception e) {
			log.info("findByAuthority", e);
		}

		return authority;
	}

	@Override
	public boolean isPasswordCheck(String login_id, String password) {
		try {
			// 패스워드 비교(userPwd를 암호화 해서 dto.getPassword()와 비교하면 안된다.)
			// password를 암호화하면 가입할때의 암호화 값과 다름. 암호화할때 마다 다른 값

			MemberDto dto = Objects.requireNonNull(findById(login_id));

			return bcryptEncoder.matches(password, dto.getPassword());
		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return false;
	}

}
