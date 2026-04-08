package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.app.domain.dto.PostDto;
import com.sp.app.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostRestController {
    private final PostService postService;

    // ── 피드 목록 조회 ──────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getPostList(
            @RequestParam(name = "styleTag",  required = false) String styleTag,
            @RequestParam(name = "gender",    required = false) String gender,
            @RequestParam(name = "season",    required = false) String season,
            @RequestParam(name = "tpo",       required = false) String tpo,
            @RequestParam(name = "loginId",   required = false) String loginId,
            @RequestParam(name = "page",      defaultValue = "1")  int page,
            @RequestParam(name = "size",      defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("styleTag",    styleTag);
            map.put("gender",      gender);
            map.put("season",      season);
            map.put("tpo",         tpo);
            map.put("loginId",     loginId);
            map.put("page",        page - 1);
            map.put("size",        size);
            map.put("currentUser", userDetails != null ? userDetails.getUsername() : null);

            return ResponseEntity.ok(postService.getPostList(map));
        } catch (Exception e) {
            log.error("getPostList error", e);
            return ResponseEntity.internalServerError().body("게시글 목록 조회 실패");
        }
    }

    // ── 게시글 상세 조회 ────────────────────────────────────────
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String currentUser = userDetails != null ? userDetails.getUsername() : null;
            PostDto dto = postService.getPost(postId, currentUser);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("getPost error", e);
            return ResponseEntity.internalServerError().body("게시글 조회 실패");
        }
    }

    // ── 게시글 작성 ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createPost(
            PostDto dto,
            @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(name = "pinsJson",   required = false) String pinsJson,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

            if (pinsJson != null && !pinsJson.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                List<PostDto.PinDto> pins = mapper.readValue(pinsJson,
                    new TypeReference<List<PostDto.PinDto>>() {});
                dto.setPins(pins);
            }

            dto.setLoginId(userDetails.getUsername());
            Long postId = postService.createPost(dto, imageFiles);
            return ResponseEntity.ok(Map.of("postId", postId));
        } catch (Exception e) {
            log.error("createPost error", e);
            return ResponseEntity.internalServerError().body("게시글 작성 실패");
        }
    }

    // ── 게시글 수정 ─────────────────────────────────────────────
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable("postId") Long postId,
            PostDto dto,
            @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(name = "pinsJson",   required = false) String pinsJson,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

            if (pinsJson != null && !pinsJson.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                List<PostDto.PinDto> pins = mapper.readValue(pinsJson,
                    new TypeReference<List<PostDto.PinDto>>() {});
                dto.setPins(pins);
            }

            dto.setLoginId(userDetails.getUsername());
            postService.updatePost(postId, dto, imageFiles);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            log.error("updatePost error", e);
            return ResponseEntity.internalServerError().body("게시글 수정 실패");
        }
    }

    // ── 게시글 삭제 ─────────────────────────────────────────────
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            postService.deletePost(postId, userDetails.getUsername());
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            log.error("deletePost error", e);
            return ResponseEntity.internalServerError().body("게시글 삭제 실패");
        }
    }

    // ── 좋아요 토글 ─────────────────────────────────────────────
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> toggleLike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            boolean liked = postService.toggleLike(postId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("liked", liked));
        } catch (Exception e) {
            log.error("toggleLike error", e);
            return ResponseEntity.internalServerError().body("좋아요 처리 실패");
        }
    }

    // ── 아카이브 토글 ───────────────────────────────────────────
    @PostMapping("/{postId}/archive")
    public ResponseEntity<?> toggleArchive(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            boolean archived = postService.toggleArchive(postId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("archived", archived));
        } catch (Exception e) {
            log.error("toggleArchive error", e);
            return ResponseEntity.internalServerError().body("아카이브 처리 실패");
        }
    }

    // ── 댓글 목록 조회 ──────────────────────────────────────────
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable("postId") Long postId) {
        try {
            return ResponseEntity.ok(postService.getComments(postId));
        } catch (Exception e) {
            log.error("getComments error", e);
            return ResponseEntity.internalServerError().body("댓글 조회 실패");
        }
    }

    // ── 댓글 작성 ───────────────────────────────────────────────
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "content") String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            postService.addComment(postId, userDetails.getUsername(), content);
            return ResponseEntity.ok("댓글 등록 완료");
        } catch (Exception e) {
            log.error("addComment error", e);
            return ResponseEntity.internalServerError().body("댓글 등록 실패");
        }
    }

    // ── 댓글 삭제 ───────────────────────────────────────────────
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("postId")    Long postId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            postService.deleteComment(commentId, userDetails.getUsername());
            return ResponseEntity.ok("댓글 삭제 완료");
        } catch (Exception e) {
            log.error("deleteComment error", e);
            return ResponseEntity.internalServerError().body("댓글 삭제 실패");
        }
    }
}