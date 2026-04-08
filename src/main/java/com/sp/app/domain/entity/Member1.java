package com.sp.app.domain.entity;

import java.time.LocalDate;

import groovy.transform.builder.Builder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="member1")
@Getter
@NoArgsConstructor
public class Member1 {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "login_id")
    private String loginId;

    private String password;

    @Column(name = "sns_provider")
    private String snsProvider;

    @Column(name = "sns_id")
    private String snsId;

    @Column(name = "userlevel")
    private int userLevel = 1;

    private int enabled = 1;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "update_at")
    private LocalDate updateAt = LocalDate.now();

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @Column(name = "failure_cnt")
    private int failureCnt = 0;
    
    @OneToOne(mappedBy = "member1", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private Member2 member2;
    
    @Builder
    public Member1(String loginId, String password, String snsProvider, String snsId) {
    	this.loginId = loginId;
    	this.password = password;
    	this.snsProvider = snsProvider;
    	this.snsId = snsId;
    }
    
}
