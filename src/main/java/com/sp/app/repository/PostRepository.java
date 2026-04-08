package com.sp.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sp.app.domain.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>,
                                        JpaSpecificationExecutor<Post> {

}