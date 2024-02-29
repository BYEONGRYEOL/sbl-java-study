package com.sparta.jpaadvance.withjisoo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.jpaadvance.withjisoo.entity.Member_Lazy;

public interface Member_LazyRepository extends JpaRepository<Member_Lazy, Long> {
	// member_lazy 테이블을 기준으로 snackList들을 가져온다.
	// SELECT DISTINCT m.*
	// FROM Member_Lazy m
	// LEFT JOIN snackLazyList s ON m.id = s.member_lazy_id;
	@Query("select distinct m from Member_Lazy m left join m.snackLazyList")
	List<Member_Lazy> findAllWithJPQL();

	@Query("select m from Member_Lazy  m left join m. snackLazyList where m.memberId = :memberId")
	Member_Lazy findByIdWithJPQL(Long memberId);

}
