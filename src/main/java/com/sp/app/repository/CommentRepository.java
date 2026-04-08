package com.sp.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
 
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
