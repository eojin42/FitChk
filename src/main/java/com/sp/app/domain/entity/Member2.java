package com.sp.app.domain.entity;

import java.time.LocalDate;

import groovy.transform.builder.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="member2")
@Getter
@NoArgsConstructor
public class Member2 {
	
	@Id
	@Column(name="member_id")
	private Long memberId;
	
	@MapsId
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="member_id")
	private Member1 member1;
	
	private String name;
	private LocalDate birth;
	
	@Column(name="profile_photo")
	private String profilePhoto;
	
	private String tel;
	
	private String zip;
    private String addr1;
    private String addr2;

    private String email;
    
    @Column(name = "receive_email")
    private int receiveEmail = 1;

    @Column(name = "ipaddr")
    private String ipAddr;
    
    @Column(name = "style_tag")
    private String styleTag;

    @Builder
    public Member2(Member1 member1, String name, String birth,
                   String tel, String zip, String addr1, String addr2,
                   String email, int receiveEmail, String ipAddr) {
        this.member1      = member1;
        this.name         = name;
        this.birth        = birth != null ? LocalDate.parse(birth) : null;
        this.tel          = tel;
        this.zip          = zip;
        this.addr1        = addr1;
        this.addr2        = addr2;
        this.email        = email;
        this.receiveEmail = receiveEmail;
        this.ipAddr       = ipAddr;
    }
}
