package com.sparta.jpaadvance.relation;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.sparta.jpaadvance.entity.OwnerOTMOneWay;
import com.sparta.jpaadvance.entity.D_OTMOneWay;
import com.sparta.jpaadvance.repository.Owner_OTMOneWayOwnerRepository;
import com.sparta.jpaadvance.repository.Dependent_OTMOneWayRepository;

import jakarta.transaction.Transactional;

@Transactional
@DataJpaTest
public class OneToManyOneWayTest {

	private final String FOOD_NAME = "foodName";
	private final String USER_NAME = "username";
	@Autowired
	Dependent_OTMOneWayRepository userRepository;
	@Autowired
	Owner_OTMOneWayOwnerRepository foodRepository;

	@Test
	@Rollback(value = false)
	@DisplayName("1대N 단방향 테스트")
	void test1() {
		D_OTMOneWay user = getUser();

		D_OTMOneWay user2 = getUser();

		OwnerOTMOneWay food = getFood();

		food.getUserList().add(user); // 외래 키(연관 관계) 설정
		food.getUserList().add(user2); // 외래 키(연관 관계) 설정

		userRepository.save(user);
		userRepository.save(user2);
		foodRepository.save(food);

		// 추가적인 UPDATE 쿼리 발생을 확인할 수 있습니다.
	}

	@Test
	@DisplayName("1대N 조회 테스트")
	void test2() {
		OwnerOTMOneWay food = foodRepository.findById(1L).orElseThrow(NullPointerException::new);
		System.out.println("food.getName() = " + food.getName());
		// 해당 음식을 주문한 고객 정보 조회
		List<D_OTMOneWay> userList = food.getUserList();
		for (D_OTMOneWay user : userList) {
			System.out.println("user.getName() = " + user.getName());
		}
	}

	OwnerOTMOneWay getFood() {
		return new OwnerOTMOneWay(FOOD_NAME, 100);
	}

	D_OTMOneWay getUser() {
		return new D_OTMOneWay(USER_NAME);
	}
}