package com.sp.app.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.StorageService;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailSender {
	private final StorageService storageService;
	
	private String mailType; // 메일 타입
	private String encType;
	private String uploadPath;
	
	@PostConstruct
	public void init() {
		this.encType = "utf-8";
		this.mailType = "text/html; charset=utf-8";
		this.uploadPath = storageService.getRealPath("/uploads/mail");
	}	

	// 네이버를 이용하여 메일을 보내는 경우 보내는사람의 이메일이 아래 계정(SMTP 권한 계정)과 다르면 메일 전송이 안된다.
	// gmail은 기본적으로 <a href ...> 태그가 있으면 href를 제거한다.
	// SMTP 권한
	private class SMTPAuthenticator extends Authenticator {
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			/*
			1) gmail
			   (1) 프로필(자기이름) 클릭 => 구글계정관리 버튼 클릭 => 좌측화면 보안 => 2단계인증 설정(전화번호등으로)
			   (2) 2단계인증 => 앱비밀번호 => App name 입력(spring) => 생성
			      앱비밀번호가 보이지 않으면 
			      "앱비밀번호"를 검색하여 구글 도움말에서 "앱비밀번호 만들기 및 관리하기" 클릭
			   (3) 패스워드 대신 2단계 인증 웹번호 입력
			2) 네이버
			   (1) 메일 아래부분 환경설정 클릭후 POP3 등을 허용
			   (2) POP3/SMTP, IMAP/SMTP 사용함 설정(나머지는 기본 설정)
		 */

			String username = "아이디"; // gmail 사용자
			// String username = "아이디@naver.com"; // 네이버 사용자;
			String password = "패스워드"; // 지메일은 앱비밀번호
			return new PasswordAuthentication(username, password);
		}
	}

	// 첨부 파일이 있는 경우 MIME을 MultiMime로 파일을 전송 한다.
	private void makeMessage(Message msg, Mail dto) throws MessagingException {
		if (dto.getSelectFile() == null || dto.getSelectFile().isEmpty()) {
			// 파일을 첨부하지 않은 경우 --
			
			msg.setContent(dto.getContent(), "text/html; charset=utf-8");
			msg.setHeader("Content-Type", mailType);
		} else {
			// 파일을 첨부하는 경우 --
			
			// 메일 내용
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setContent(dto.getContent(), "text/html; charset=utf-8");
			mbp1.setHeader("Content-Type", mailType);

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);

			// 첨부 파일
			for (MultipartFile mf : dto.getSelectFile()) {
				if (mf.isEmpty()) {
					continue;
				}

				try {
					String saveFilename = storageService.uploadFileToServer(mf, uploadPath);
					if (saveFilename != null) {
						dto.getSavePathname().add(uploadPath + File.separator + saveFilename);

						String originalFilename = mf.getOriginalFilename();
						MimeBodyPart mbp2 = new MimeBodyPart();
						FileDataSource fds = new FileDataSource(uploadPath + File.separator + saveFilename);
						mbp2.setDataHandler(new DataHandler(fds));

						if (originalFilename == null || originalFilename.isBlank()) {
							mbp2.setFileName(MimeUtility.encodeWord(fds.getName()));
						} else {
							mbp2.setFileName(MimeUtility.encodeWord(originalFilename));
						}
						
						mp.addBodyPart(mbp2);
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			msg.setContent(mp);
		}
	}

	public boolean mailSend(Mail dto) {
		boolean b = false;

		Properties p = new Properties();

		// SMTP 서버의 계정 설정
		p.setProperty("mail.smtp.user", "아이디"); // 지메일 또는 네이버 아이디

		// SMTP 서버 정보 설정
		String host = "smtp.gmail.com"; // gmail
		// String host = "smtp.naver.com"; // 네이버
		p.setProperty("mail.smtp.host", host);

		// 네이버와 지메일 동일 ---
		// 메일 포트
		p.setProperty("mail.smtp.port", "465");

		// 메일은 보안 연결에 SSL 또는 TLS 사용
		p.setProperty("mail.smtp.starttls.enable", "true");
				// TLS 보호 연결 활성화(일부 SMTP 서버에서 TLS 연결이 필요하므로) 
		p.setProperty("mail.smtp.ssl.enable", "true");
				// SMTP 연결에 SSL을 사용하는 경우(보통 포트가 465)
		
		p.setProperty("mail.smtp.auth", "true");
		// SMTP 인증 사용시 true로 설정

		// 소켓 팩토리 관련 설정
		p.setProperty("mail.smtp.socketFactory.port", "465");
		p.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		p.setProperty("mail.smtp.socketFactory.fallback", "false");
		
		// JDK 8, 17, 21 등에서 다음의 에러가 발생하는 경우 추가(JDK 14는 에러가 발생하지 않으므로 주석처리)
		/*
		javax.mail.MessagingException: Could not connect to SMTP host: smtp.gmail.com, port: 465;
		nested exception is:
			javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)
		 */
		p.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
		// p.setProperty("mail.smtp.ssl.trust", host);
		p.setProperty("mail.smtp.ssl.trust", "*");
		
		try {
			Authenticator auth = new SMTPAuthenticator();
			Session session = Session.getDefaultInstance(p, auth);
			
			// 메일 전송시 상세 정보 콘솔에 출력 여부
			// session.setDebug(true);

			Message msg = new MimeMessage(session);

			// 보내는 사람
			if (dto.getSenderName() == null || dto.getSenderName().isEmpty()) {
				msg.setFrom(new InternetAddress(dto.getSenderEmail()));
			} else {
				msg.setFrom(new InternetAddress(dto.getSenderEmail(), dto.getSenderName(), encType));
			}

			// 받는 사람(한명)
			// msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dto.getReceiverEmail()));
			
			// 받는사람(여러명 가능)
			String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
			String[] receiverEmails = dto.getReceiverEmail().trim().split(";");
			List<String> list = new ArrayList<>();
			for(String email : receiverEmails) {
				if(email.isBlank()) {
					continue;
				}
				
				if(Pattern.matches(regex, email.trim())) {
					list.add(email.trim());
				} else {
					return false;
				}
			}
			if(list.size() == 0) {
				return false;
			}
			Address[] addresses = new Address[list.size()];
			for(int i = 0; i < list.size(); i++) {
				addresses[i] = new InternetAddress(list.get(i));
				// addresses[i] = new InternetAddress("수신자이메일1", "수신자이름1", encType);
			}
			msg.setRecipients(Message.RecipientType.TO, addresses);

			// 제목
			msg.setSubject(dto.getSubject());
			
			// 메일 내용
			makeMessage(msg, dto);
			
			// 메일 보낸 사람
			msg.setHeader("X-Mailer", dto.getSenderName());

			// 메일 보낸 날짜
			msg.setSentDate(new Date());

			// 메일 전송
			Transport.send(msg);

			// 메일 전송후 서버에 저장된 첨부 파일 삭제
			if (dto.getSavePathname() != null && dto.getSavePathname().size() > 0) {
				for (String filename : dto.getSavePathname()) {
					File file = new File(filename);
					if (file.exists()) {
						file.delete();
					}
				}
			}

			b = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return b;
	}
}
