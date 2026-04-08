package com.sp.app.repository;

import com.sp.app.domain.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPostPostIdOrderBySortOrderAsc(Long postId);
    void deleteByPostPostId(Long postId);
}