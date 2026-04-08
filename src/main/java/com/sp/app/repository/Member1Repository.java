package com.sp.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.Member1;

public interface Member1Repository extends JpaRepository<Member1, Long>{
	boolean existsByLoginId(String loginId);
	
	Optional<Member1> findByLoginId(String loginId);
	Optional<Member1> findBySnsProviderAndSnsId(String snsProvider, String snsId);
}
