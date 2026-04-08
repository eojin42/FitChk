package com.sp.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.Member2;

public interface Member2Repository extends JpaRepository<Member2, Long>{
	boolean existsByEmail(String email);
}
