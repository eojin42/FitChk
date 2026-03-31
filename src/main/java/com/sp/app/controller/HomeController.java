package com.sp.app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
	@GetMapping(value = {"/", ""})
	public String handleHome(@AuthenticationPrincipal UserDetails userDetails) {
		
		if(userDetails != null) {
			return "redirect:/admin";
		}		
		
		return "member/login2";
	}
}
