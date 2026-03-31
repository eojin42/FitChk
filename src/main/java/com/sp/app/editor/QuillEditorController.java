package com.sp.app.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.StorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Quill(퀼) text editor 이미지 업로드
@RestController
@RequiredArgsConstructor
@Slf4j
public class QuillEditorController {
	private final StorageService storageService;

	@Value("${file.upload-root}/editor")
	private String uploadPath;
	
	@PostMapping(value = {"/editor/upload", "/api/editor/upload"})
	public ResponseEntity<?> handleImageUpload(@RequestParam(name = "imageFile") MultipartFile partFile,
			HttpServletRequest req) {
		// Quill(퀼) text editor
		
		try {
			String saveFilename = Objects.requireNonNull(storageService.uploadFileToServer(partFile, uploadPath));
			
			String scheme  = req.getScheme();
			String serverName = req.getServerName();
			int serverPort = req.getServerPort();
			String cp = req.getContextPath();
			String baseUrl = scheme + "://" + serverName + ":" + serverPort + cp;
			String imageUrl = baseUrl + "/uploads/editor/" + saveFilename;
			
			Map<String, Object> model = new HashMap<>();
			model.put("saveFilename", saveFilename);
			model.put("imageUrl", imageUrl);
			
			return ResponseEntity.ok(model);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
