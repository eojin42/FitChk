package com.sp.app.domain.entity;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "POSTS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq")
    @SequenceGenerator(name = "posts_seq", sequenceName = "POSTS_SEQ", allocationSize = 1)
    @Column(name = "POST_ID")
    private Long postId;

    // 작성자 
    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    // 본문
    @Column(name = "CONTENT", length = 2000)
    private String content;

    // 대표 이미지 경로
    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    // 성별
    @Column(name = "GENDER", length = 10)
    private String gender;

    // 계절 (콤마 구분 문자열 ex. "봄,가을")
    @Column(name = "SEASONS", length = 50)
    private String seasons;

    // 스타일 태그 (콤마 구분 문자열 ex. "스트릿,캐주얼")
    @Column(name = "STYLE_TAGS", length = 100)
    private String styleTags;

    // TPO (콤마 구분 문자열 ex. "데일리,출근")
    @Column(name = "TPO_TAGS", length = 100)
    private String tpoTags;

    // 체형 정보
    @Column(name = "HEIGHT")
    private Double height;

    @Column(name = "WEIGHT")
    private Double weight;

    // 좋아요 수
    @Column(name = "LIKE_COUNT", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    // 댓글 수
    @Column(name = "COMMENT_COUNT", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    // 등록일 / 수정일
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // 핀 아이템 (1:N)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<PostItem> postItems = new ArrayList<>();

    // 좋아요 수 증가/감소
    public void increaseLikeCount() { this.likeCount++; }
    public void decreaseLikeCount() { if (this.likeCount > 0) this.likeCount--; }

    // 댓글 수 증가/감소
    public void increaseCommentCount() { this.commentCount++; }
    public void decreaseCommentCount() { if (this.commentCount > 0) this.commentCount--; }
}