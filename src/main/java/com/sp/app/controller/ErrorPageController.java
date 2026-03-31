package com.sp.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ErrorPageController {

	@GetMapping("/error/error")
	public String handleError() {
		return "error/error";
	}
	
	@GetMapping("/error/downloadFailed")
	public String handleDownloadFailed() {
		return "error/downloadFailure";
	}
	
	@GetMapping("/admin/error/downloadFailed")
	public String handleAdminDownloadFailed() {
		return "admin/error/downloadFailure";
	}
}
