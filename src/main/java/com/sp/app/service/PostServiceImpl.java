package com.sp.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.PostDto;
import com.sp.app.domain.dto.PostDto.CommentDto;
import com.sp.app.domain.dto.PostDto.PinDto;
import com.sp.app.domain.entity.Archive;
import com.sp.app.domain.entity.Comment;
import com.sp.app.domain.entity.Like;
import com.sp.app.domain.entity.Post;
import com.sp.app.domain.entity.PostImage;
import com.sp.app.domain.entity.PostItem;
import com.sp.app.repository.ArchiveRepository;
import com.sp.app.repository.CommentRepository;
import com.sp.app.repository.LikeRepository;
import com.sp.app.repository.PostImageRepository;
import com.sp.app.repository.PostRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository      postRepository;
    private final PostImageRepository postImageRepository;
    private final LikeRepository      likeRepository;
    private final ArchiveRepository   archiveRepository;
    private final CommentRepository   commentRepository;
    private final MemberService       memberService;
    private final StorageService      storageService;

    @Value("${file.upload-root}/post")
    private String uploadPath;

    // ── 게시글 목록 ──────────────────────────────
    @Override
    public Map<String, Object> getPostList(Map<String, Object> map) throws Exception {
        String styleTag    = (String) map.get("styleTag");
        String gender      = (String) map.get("gender");
        String season      = (String) map.get("season");
        String tpo         = (String) map.get("tpo");
        String loginId     = (String) map.get("loginId");
        String currentUser = (String) map.get("currentUser");
        int    page        = (int)    map.get("page");
        int    size        = (int)    map.get("size");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (loginId != null && !loginId.isBlank()) {
            MemberDto member = memberService.findById(loginId);
            if (member == null) return Map.of("list", List.of(), "totalCount", 0);
            Long memberId = member.getMember_id();
            Specification<Post> spec = (root, q, cb) -> cb.equal(root.get("memberId"), memberId);
            return toResult(postRepository.findAll(spec, pageable), currentUser);
        }

        Specification<Post> spec = (root, q, cb) -> {
            Predicate predicate = cb.conjunction();

            if (gender != null && !gender.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("gender"), gender));
            }

            if (styleTag != null && !styleTag.isBlank()) {
                String[] tags = styleTag.split(",");
                Predicate tagPred = cb.disjunction();
                for (String tag : tags) {
                    String t = tag.trim();
                    tagPred = cb.or(tagPred,
                        cb.equal(root.get("styleTags"), t),
                        cb.like(root.get("styleTags"), t + ",%"),
                        cb.like(root.get("styleTags"), "%," + t),
                        cb.like(root.get("styleTags"), "%," + t + ",%")
                    );
                }
                predicate = cb.and(predicate, tagPred);
            }

            if (season != null && !season.isBlank()) {
                String[] seasons = season.split(",");
                Predicate seasonPred = cb.disjunction();
                for (String s : seasons) {
                    String sv = s.trim();
                    seasonPred = cb.or(seasonPred,
                        cb.equal(root.get("seasons"), sv),
                        cb.like(root.get("seasons"), sv + ",%"),
                        cb.like(root.get("seasons"), "%," + sv),
                        cb.like(root.get("seasons"), "%," + sv + ",%")
                    );
                }
                predicate = cb.and(predicate, seasonPred);
            }

            if (tpo != null && !tpo.isBlank()) {
                String[] tpos = tpo.split(",");
                Predicate tpoPred = cb.disjunction();
                for (String t : tpos) {
                    String tv = t.trim();
                    tpoPred = cb.or(tpoPred,
                        cb.equal(root.get("tpoTags"), tv),
                        cb.like(root.get("tpoTags"), tv + ",%"),
                        cb.like(root.get("tpoTags"), "%," + tv),
                        cb.like(root.get("tpoTags"), "%," + tv + ",%")
                    );
                }
                predicate = cb.and(predicate, tpoPred);
            }

            return predicate;
        };

        return toResult(postRepository.findAll(spec, pageable), currentUser);
    }

    private Map<String, Object> toResult(Page<Post> postPage, String currentUser) {
        List<PostDto> list = postPage.getContent().stream()
                .map(post -> toDto(post, currentUser))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("list",       list);
        result.put("totalCount", (int) postPage.getTotalElements());
        return result;
    }

    // ── 게시글 상세 ──────────────────────────────
    @Override
    public PostDto getPost(Long postId, String currentUser) throws Exception {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return null;
        return toDto(post, currentUser);
    }

    // ── 게시글 작성 ──────────────────────────────
    @Override
    @Transactional
    public Long createPost(PostDto dto, List<MultipartFile> imageFiles) throws Exception {
        MemberDto member = memberService.findById(dto.getLoginId());
        if (member == null) throw new RuntimeException("회원 정보를 찾을 수 없습니다.");

        // 대표 이미지 (첫 번째)
        String mainImageUrl = null;
        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
            mainImageUrl = storageService.uploadFileToServer(imageFiles.get(0), uploadPath);
        }

        Post post = Post.builder()
                .memberId(member.getMember_id())
                .content(dto.getContent())
                .imageUrl(mainImageUrl)
                .gender(dto.getGender())
                .seasons(dto.getSeasons())
                .styleTags(dto.getStyleTags())
                .tpoTags(dto.getTpoTags())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .likeCount(0)
                .commentCount(0)
                .build();

        // 핀
        if (dto.getPins() != null) {
            for (PinDto pin : dto.getPins()) {
                PostItem item = PostItem.builder()
                        .post(post)
                        .brand(pin.getBrand())
                        .productName(pin.getProductName())
                        .price(pin.getPrice())
                        .purchaseUrl(pin.getPurchaseUrl())
                        .posX(pin.getPosX())
                        .posY(pin.getPosY())
                        .build();
                post.getPostItems().add(item);
            }
        }

        Post saved = postRepository.save(post);

        // 추가 이미지 저장 (2번째부터)
        if (imageFiles != null && imageFiles.size() > 1) {
            for (int i = 1; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String url = storageService.uploadFileToServer(file, uploadPath);
                    postImageRepository.save(PostImage.builder()
                            .post(saved)
                            .imageUrl(url)
                            .sortOrder(i)
                            .build());
                }
            }
        }

        return saved.getPostId();
    }

    // ── 게시글 수정 ──────────────────────────────
    @Override
    @Transactional
    public void updatePost(Long postId, PostDto dto, List<MultipartFile> imageFiles) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        MemberDto member = memberService.findById(dto.getLoginId());
        if (member == null || !post.getMemberId().equals(member.getMember_id())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 내용 업데이트
        post.setContent(dto.getContent());
        if (dto.getGender()    != null) post.setGender(dto.getGender());
        if (dto.getSeasons()   != null) post.setSeasons(dto.getSeasons());
        if (dto.getStyleTags() != null) post.setStyleTags(dto.getStyleTags());
        if (dto.getTpoTags()   != null) post.setTpoTags(dto.getTpoTags());
        if (dto.getHeight()    != null) post.setHeight(dto.getHeight());
        if (dto.getWeight()    != null) post.setWeight(dto.getWeight());

        // 새 이미지가 있으면 교체
        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
            // 기존 대표 이미지 삭제
            if (post.getImageUrl() != null) storageService.deleteFile(uploadPath, post.getImageUrl());
            // 기존 추가 이미지 삭제
            postImageRepository.findByPostPostIdOrderBySortOrderAsc(postId)
                    .forEach(img -> storageService.deleteFile(uploadPath, img.getImageUrl()));
            postImageRepository.deleteByPostPostId(postId);

            // 새 이미지 저장
            String mainUrl = storageService.uploadFileToServer(imageFiles.get(0), uploadPath);
            post.setImageUrl(mainUrl);

            for (int i = 1; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String url = storageService.uploadFileToServer(file, uploadPath);
                    postImageRepository.save(PostImage.builder()
                            .post(post).imageUrl(url).sortOrder(i).build());
                }
            }
        }

        postRepository.save(post);
    }

    // ── 게시글 삭제 ──────────────────────────────
    @Override
    @Transactional
    public void deletePost(Long postId, String loginId) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        MemberDto member = memberService.findById(loginId);
        if (member == null || !post.getMemberId().equals(member.getMember_id())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        // 이미지 파일 삭제
        if (post.getImageUrl() != null && !post.getImageUrl().isBlank()) {
            storageService.deleteFile(uploadPath, post.getImageUrl());
        }
        postImageRepository.findByPostPostIdOrderBySortOrderAsc(postId).forEach(img ->
            storageService.deleteFile(uploadPath, img.getImageUrl())
        );

        postRepository.delete(post);
    }

    // ── 좋아요 토글 ──────────────────────────────
    @Override
    @Transactional
    public boolean toggleLike(Long postId, String loginId) throws Exception {
        Optional<Like> existing = likeRepository.findByPostIdAndLoginId(postId, loginId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            post.decreaseLikeCount();
            postRepository.save(post);
            return false;
        } else {
            likeRepository.save(Like.builder().postId(postId).loginId(loginId).build());
            post.increaseLikeCount();
            postRepository.save(post);
            return true;
        }
    }

    // ── 아카이브 토글 ────────────────────────────
    @Override
    @Transactional
    public boolean toggleArchive(Long postId, String loginId) throws Exception {
        Optional<Archive> existing = archiveRepository.findByPostIdAndLoginId(postId, loginId);
        if (existing.isPresent()) {
            archiveRepository.delete(existing.get());
            return false;
        } else {
            archiveRepository.save(Archive.builder().postId(postId).loginId(loginId).build());
            return true;
        }
    }

    // ── 댓글 목록 ────────────────────────────────
    @Override
    public List<CommentDto> getComments(Long postId) throws Exception {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(c -> {
                    MemberDto member = memberService.findById(c.getLoginId());
                    return CommentDto.builder()
                            .commentId(c.getCommentId())
                            .postId(c.getPostId())
                            .loginId(c.getLoginId())
                            .nickname(member != null ? member.getLogin_id() : c.getLoginId())
                            .profileImage(member != null ? member.getProfile_photo() : null)
                            .content(c.getContent())
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── 댓글 작성 ────────────────────────────────
    @Override
    @Transactional
    public void addComment(Long postId, String loginId, String content) throws Exception {
        commentRepository.save(Comment.builder()
                .postId(postId).loginId(loginId).content(content).build());
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.increaseCommentCount();
        postRepository.save(post);
    }

    // ── 댓글 삭제 ────────────────────────────────
    @Override
    @Transactional
    public void deleteComment(Long commentId, String loginId) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        if (!comment.getLoginId().equals(loginId)) throw new RuntimeException("삭제 권한이 없습니다.");

        commentRepository.delete(comment);
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.decreaseCommentCount();
        postRepository.save(post);
    }

    // ── Entity → DTO ─────────────────────────────
    private PostDto toDto(Post post, String currentUser) {
        MemberDto member = memberService.findById(post.getMemberId());

        boolean liked    = currentUser != null &&
                likeRepository.existsByPostIdAndLoginId(post.getPostId(), currentUser);
        boolean archived = currentUser != null &&
                archiveRepository.existsByPostIdAndLoginId(post.getPostId(), currentUser);

        List<PinDto> pins = post.getPostItems().stream()
                .map(item -> PinDto.builder()
                        .itemId(item.getItemId())
                        .brand(item.getBrand())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .purchaseUrl(item.getPurchaseUrl())
                        .posX(item.getPosX())
                        .posY(item.getPosY())
                        .build())
                .collect(Collectors.toList());

        // 이미지 URL 목록: 대표 이미지 + 추가 이미지
        List<String> imageUrls = new ArrayList<>();
        if (post.getImageUrl() != null && !post.getImageUrl().isBlank()) {
            imageUrls.add(post.getImageUrl());
        }
        postImageRepository.findByPostPostIdOrderBySortOrderAsc(post.getPostId())
                .forEach(img -> imageUrls.add(img.getImageUrl()));

        return PostDto.builder()
                .postId(post.getPostId())
                .loginId(member != null ? member.getLogin_id() : null)
                .nickname(member != null ? member.getLogin_id() : "알 수 없음")
                .profileImage(member != null ? member.getProfile_photo() : null)
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .imageUrls(imageUrls)
                .gender(post.getGender())
                .seasons(post.getSeasons())
                .styleTags(post.getStyleTags())
                .tpoTags(post.getTpoTags())
                .height(post.getHeight())
                .weight(post.getWeight())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .liked(liked)
                .archived(archived)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .pins(pins)
                .build();
    }
}