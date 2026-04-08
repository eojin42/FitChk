package com.sp.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_image")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_image_seq")
    @SequenceGenerator(name = "post_image_seq", sequenceName = "post_image_seq", allocationSize = 1)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}