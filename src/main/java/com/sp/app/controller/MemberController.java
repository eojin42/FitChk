package com.sp.app.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.common.RequestUtils;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member/*")
public class MemberController {
    private final MemberService memberService;

    @Value("${file.upload-root}/member")
    private String uploadPath;

    // 로그인 폼 (타임리프/관리자용)
    @RequestMapping(value = "login", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginForm(@RequestParam(name = "error", required = false) String error,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails != null) return "redirect:/admin";
        if (error != null) model.addAttribute("message", "아이디 또는 패스워드가 일치하지 않습니다.");
        return "member/login2";
    }

    // 회원가입
    @GetMapping("account")
    public String memberForm(Model model) {
        model.addAttribute("mode", "account");
        return "member/member";
    }

    @ResponseBody
    @PostMapping("account")
    public ResponseEntity<?> memberSubmit(@ModelAttribute MemberDto dto) {
        try {
            dto.setIpAddr(RequestUtils.getClientIp());
            memberService.insertMember(dto, uploadPath);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (DuplicateKeyException e) {
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("입력 정보를 확인해주세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("회원가입에 실패했습니다.");
        }
    }

    // 아이디 중복확인
    @ResponseBody
    @PostMapping("userIdCheck")
    public Map<String, ?> handleUserIdCheck(@RequestParam(name = "login_id") String login_id) {
        Map<String, Object> result = new HashMap<>();
        String p = "false";
        try {
            MemberDto dto = memberService.findById(login_id);
            if (dto == null) p = "true";
        } catch (Exception e) {}
        result.put("passed", p);
        return result;
    }

    // 내 정보 조회
    @ResponseBody
    @GetMapping("me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String loginId = userDetails.getUsername();
            MemberDto dto = Objects.requireNonNull(memberService.findById(loginId));
            dto.setPassword(null); // 패스워드 제거 후 반환
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
    }

    // 회원정보 수정
    @ResponseBody
    @PostMapping("update")
    public ResponseEntity<?> updateSubmit(@ModelAttribute MemberDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String loginId = userDetails.getUsername();
            MemberDto myDto = Objects.requireNonNull(memberService.findById(loginId));
            dto.setMember_id(myDto.getMember_id());
            dto.setLogin_id(loginId);
            memberService.updateMember(dto, uploadPath);
            return ResponseEntity.ok("회원정보가 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("회원정보 변경에 실패했습니다.");
        }
    }

    // 비밀번호 확인 (수정/탈퇴 전)
    @ResponseBody
    @PostMapping("pwd")
    public ResponseEntity<String> pwdSubmit(
            @RequestParam(name = "password") String password,
            @RequestParam(name = "mode") String mode,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String loginId = userDetails.getUsername();
            MemberDto dto = Objects.requireNonNull(memberService.findById(loginId));

            if (!memberService.isPasswordCheck(loginId, password)) {
                return ResponseEntity.badRequest().body("패스워드가 일치하지 않습니다.");
            }

            if (mode.equals("dropout")) {
                Map<String, Object> map = new HashMap<>();
                map.put("login_id", loginId);
                map.put("filename", dto.getProfile_photo());
                memberService.deleteMember(map, uploadPath);
                // 로그아웃은 클라이언트에서 토큰 삭제로 처리
                return ResponseEntity.ok("탈퇴 처리가 완료됐습니다.");
            }

            return ResponseEntity.ok("확인 완료");
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body("회원 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류가 발생했습니다.");
        }
    }

    // 비밀번호 찾기 
    @ResponseBody
    @PostMapping("pwdFind")
    public ResponseEntity<?> pwdFind(@RequestParam(name = "login_id") String login_id) {
        try {
            MemberDto dto = memberService.findById(login_id);
            if (dto == null || dto.getEmail() == null || dto.getEnabled() == 0) {
                return ResponseEntity.badRequest().body("등록된 아이디가 아닙니다.");
            }
            memberService.generatePwd(dto);
            return ResponseEntity.ok("이메일로 임시 패스워드를 전송했습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("이메일 전송에 실패했습니다.");
        }
    }

    // 프로필 사진 삭제
    @ResponseBody
    @DeleteMapping("deleteProfile")
    public ResponseEntity<?> deleteProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String loginId = userDetails.getUsername();
            MemberDto dto = Objects.requireNonNull(memberService.findById(loginId));
            if (dto.getProfile_photo() != null && !dto.getProfile_photo().isBlank()) {
                Map<String, Object> map = new HashMap<>();
                map.put("login_id", loginId);
                map.put("filename", dto.getProfile_photo());
                memberService.deleteProfilePhoto(map, uploadPath);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 기타
    @GetMapping("complete")
    public String complete(@ModelAttribute("message") String message) throws Exception {
        if (message == null || message.isBlank()) return "redirect:/";
        return "member/complete";
    }

    @GetMapping("expired")
    public String expired() { return "member/expired"; }

    @GetMapping("noAuthorized")
    public String noAuthorized() { return "member/noAuthorized"; }
}