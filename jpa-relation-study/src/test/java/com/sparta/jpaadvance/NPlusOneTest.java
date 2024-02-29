package com.sparta.jpaadvance;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.jpaadvance.withjisoo.entity.Member_Eager;
import com.sparta.jpaadvance.withjisoo.entity.Member_Lazy;
import com.sparta.jpaadvance.withjisoo.entity.Snack_Eager;
import com.sparta.jpaadvance.withjisoo.entity.Snack_Lazy;
import com.sparta.jpaadvance.withjisoo.repository.Member_EagerRepository;
import com.sparta.jpaadvance.withjisoo.repository.Member_LazyRepository;
import com.sparta.jpaadvance.withjisoo.repository.Snack_EagerRepository;
import com.sparta.jpaadvance.withjisoo.repository.Snack_LazyRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
public class NPlusOneTest {

	@Autowired
	Member_LazyRepository memberLazyRepository;

	@Autowired
	Snack_LazyRepository snackLazyRepository;

	@Autowired
	Member_EagerRepository memberEagerRepository;
	@Autowired
	Snack_EagerRepository snackEagerRepository;

	@PersistenceContext
	EntityManager entityManager;

	@BeforeEach
	void beforeEach() {
		initEagers();
		initLazies();
	}

	@Test
	@Rollback(value = false)
	void t() {
		int snackIndex = 1;
		for (int i = 1; i <= 10; i++) {
			Member_Lazy memberLazy = getMemberLazy("멤버" + i);
			List<Snack_Lazy> snackLazyList = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				Snack_Lazy snackLazy = getSnackLazy("과자" + snackIndex++);
				snackLazyList.add(snackLazy);
				snackLazy.setMemberLazy(memberLazy);
			}
			memberLazy.setSnackLazyList(snackLazyList);
			memberLazyRepository.save(memberLazy);
			snackLazyRepository.saveAll(snackLazyList);
		}
	}

	@Test
	@Transactional
	void eagerNPlusOne() {
		entityManager.flush();
		entityManager.clear();
		printWithDivisor("Eager Loading인 케이스 oneToMany 연관관계의 주인인 Member를 전체 조회");
		printWithDivisor("Eager Loading 이기 때문에 member와 관계를 맺는 Snack 객체의 사용과 관계없이 N+1 문제 발생");
		printWithDivisor("예상 N+1 발생 지점");
		List<Member_Eager> findMemberEagers = memberEagerRepository.findAll();
	}

	@Test
	@Transactional
	void lazyNPlusOne() {
		entityManager.flush();
		entityManager.clear();
		printWithDivisor("Lazy Loading인 케이스 oneToMany 연관관계의 주인인 Member를 전체 조회");
		List<Member_Lazy> findMemberLazies = memberLazyRepository.findAll();
		printWithDivisor("이제 조회된 member들과 일대다 관계를 맺고있는 스낵 객체들을 사용할 것임");
		printWithDivisor("예상 N+1 발생 지점");
		findMemberLazies.stream()
			.forEach(memberLazy -> memberLazy.getSnackLazyList()
				.forEach(snackLazy -> System.out.println(snackLazy.getName())));
	}

	@Test
	void jpqlFailTest() {
		printWithDivisor("Lazy Loading인 케이스 oneToMany 연관관계의 주인인 Member를 JPQL을 이용해 전체 조회");
		List<Member_Lazy> findMemberLaziesWithJPQL = memberLazyRepository.findAllWithJPQL();
		printWithDivisor("예상 N+1 발생 지점");
		findMemberLaziesWithJPQL.stream()
			.forEach(memberLazy -> memberLazy.getSnackLazyList()
				.stream()
				.forEach(snackLazy -> System.out.println(snackLazy.getId())));
	}

	@Test
	void jpqltest2() {
		printWithDivisor("lazy loading 특정 멤버를 JPQL을 이용해 조회");
		Member_Lazy findMember = memberLazyRepository.findByIdWithJPQL(8L);
		printWithDivisor("예상 N+1 발생 지점");
		findMember.getSnackLazyList().size();
	}

	void printWithDivisor(String message) {
		System.out.println();
		System.out.println("----------------" + message + "-------------------");
		System.out.println();
	}

	void initEagers() {
		int snackIndex = 1;
		for (int i = 1; i <= 10; i++) {
			Member_Eager memberEager = getMemberEager("멤버" + i);
			List<Snack_Eager> snackEagerList = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				Snack_Eager snackEager = getSnackEager("과자" + snackIndex++);
				snackEager.setMemberEager(memberEager);
				snackEagerList.add(snackEager);
			}
			memberEager.setSnackEagerList(snackEagerList);
			memberEagerRepository.save(memberEager);
			snackEagerRepository.saveAll(snackEagerList);
		}
	}

	void initLazies() {
		int snackIndex = 1;
		for (int i = 1; i <= 10; i++) {
			Member_Lazy memberLazy = getMemberLazy("멤버" + i);
			List<Snack_Lazy> snackLazyList = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				Snack_Lazy snackLazy = getSnackLazy("과자" + snackIndex++);
				snackLazyList.add(snackLazy);
				snackLazy.setMemberLazy(memberLazy);
			}
			memberLazy.setSnackLazyList(snackLazyList);
			memberLazyRepository.save(memberLazy);
			snackLazyRepository.saveAll(snackLazyList);
		}
	}

	private Snack_Lazy getSnackLazy(String name) {
		return new Snack_Lazy(name);
	}

	private Member_Lazy getMemberLazy(String name) {
		return new Member_Lazy(name);
	}

	private Snack_Eager getSnackEager(String name) {
		return new Snack_Eager(name);
	}

	private Member_Eager getMemberEager(String name) {
		return new Member_Eager(name);
	}
}
