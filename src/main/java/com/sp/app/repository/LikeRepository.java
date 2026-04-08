package com.sp.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {
 
    Optional<Like> findByPostIdAndLoginId(Long postId, String loginId);
 
    boolean existsByPostIdAndLoginId(Long postId, String loginId);
}
