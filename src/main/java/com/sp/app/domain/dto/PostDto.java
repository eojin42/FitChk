package com.sp.app.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
	private Long          postId;
    private String        loginId;
    private String        nickname;
    private String        profileImage;
    private String        content;
    private String        imageUrl;
    private List<String> imageUrls;
 
    private String  gender;
    private String  seasons;
    private String  styleTags;
    private String  tpoTags;
    private Double  height;
    private Double  weight;
 
    private Integer likeCount;
    private Integer commentCount;
 
    private boolean liked;
    private boolean archived;
 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    private List<PinDto> pins;
 
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PinDto {
        private Long    itemId;
        private String  brand;
        private String  productName;
        private Integer price;
        private String  purchaseUrl;
        private Double  posX;
        private Double  posY;
    }
 
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentDto {
        private Long          commentId;
        private Long          postId;
        private String        loginId;
        private String        nickname;
        private String        profileImage;
        private String        content;
        private LocalDateTime createdAt;
        private boolean       isMine;
    }
}
