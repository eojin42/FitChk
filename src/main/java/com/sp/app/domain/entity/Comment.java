package com.sp.app.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "COMMENTS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_seq")
    @SequenceGenerator(name = "comments_seq", sequenceName = "COMMENTS_SEQ", allocationSize = 1)
    @Column(name = "COMMENT_ID")
    private Long commentId;
 
    @Column(name = "POST_ID", nullable = false)
    private Long postId;
 
    @Column(name = "LOGIN_ID", nullable = false, length = 50)
    private String loginId;
 
    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
 
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
