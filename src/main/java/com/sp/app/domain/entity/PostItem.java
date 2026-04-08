package com.sp.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "POST_ITEMS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostItem {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_items_seq")
	    @SequenceGenerator(name = "post_items_seq", sequenceName = "POST_ITEMS_SEQ", allocationSize = 1)
	    @Column(name = "ITEM_ID")
	    private Long itemId;
	 
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "POST_ID", nullable = false)
	    private Post post;
	 
	    @Column(name = "BRAND", length = 100)
	    private String brand;
	 
	    @Column(name = "PRODUCT_NAME", length = 200)
	    private String productName;
	 
	    @Column(name = "PRICE")
	    private Integer price;
	 
	    @Column(name = "PURCHASE_URL", length = 500)
	    private String purchaseUrl;
	 
	    // 사진 위 핀 위치 (%)
	    @Column(name = "POS_X")
	    private Double posX;
	 
	    @Column(name = "POS_Y")
	    private Double posY;
}
