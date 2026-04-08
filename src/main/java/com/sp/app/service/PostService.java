package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.sp.app.domain.dto.PostDto;
import com.sp.app.domain.dto.PostDto.CommentDto;

public interface PostService {
	public Map<String, Object> getPostList(Map<String, Object> map) throws Exception;
    public PostDto getPost(Long postId, String currentUser) throws Exception;
    public Long createPost(PostDto dto, List<MultipartFile> imageFiles) throws Exception;
    public void deletePost(Long postId, String loginId) throws Exception; 
    public void updatePost(Long postId, PostDto dto, List<MultipartFile> imageFiles) throws Exception; 
    public boolean toggleLike(Long postId, String loginId) throws Exception;
    public boolean toggleArchive(Long postId, String loginId) throws Exception;
    public List<CommentDto> getComments(Long postId) throws Exception;
    public void addComment(Long postId, String loginId, String content) throws Exception;
    public void deleteComment(Long commentId, String loginId) throws Exception; 
	
}
