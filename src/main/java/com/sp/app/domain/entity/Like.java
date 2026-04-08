package com.sp.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "LIKES",
       uniqueConstraints = @UniqueConstraint(columnNames = {"POST_ID", "MEMBER_ID"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {
 
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "likes_seq")
    @SequenceGenerator(name = "likes_seq", sequenceName = "LIKES_SEQ", allocationSize = 1)
    @Column(name = "LIKE_ID")
    private Long likeId;
 
    @Column(name = "POST_ID", nullable = false)
    private Long postId;
 
    @Column(name = "LOGIN_ID", nullable = false)
    private String loginId;
 
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}