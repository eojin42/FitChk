package com.sp.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.Archive;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {
 
    Optional<Archive> findByPostIdAndLoginId(Long postId, String loginId);
 
    boolean existsByPostIdAndLoginId(Long postId, String loginId);
}