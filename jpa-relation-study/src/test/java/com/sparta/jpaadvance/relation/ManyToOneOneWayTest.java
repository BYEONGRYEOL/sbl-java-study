package com.sparta.jpaadvance.relation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.jpaadvance.entity.OwnerMTOOneWay;
import com.sparta.jpaadvance.entity.D_MTOOneWay;
import com.sparta.jpaadvance.repository.Owner_MTOOneWayRepository;
import com.sparta.jpaadvance.repository.Dependent_MTOOneWayRepository;

@Transactional
@DataJpaTest
public class ManyToOneOneWayTest {

	private final String FOOD_NAME = "foodName";
	private final String USER_NAME = "username";
	@Autowired
	Dependent_MTOOneWayRepository userRepository;
	@Autowired
	Owner_MTOOneWayRepository foodRepository;

	@Test
	@Rollback(value = false)
	@DisplayName("N대1 단방향 테스트")
	void insertManyRelatedByOneTest() {
		D_MTOOneWay user = new D_MTOOneWay();
		user.setName("Robbie");

		OwnerMTOOneWay food = getFood();
		food.setUser(user); // 외래 키(연관 관계) 설정

		OwnerMTOOneWay food2 = getFood();
		food2.setUser(user); // 외래 키(연관 관계) 설정

		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);
	}

	OwnerMTOOneWay getFood() {
		return new OwnerMTOOneWay(FOOD_NAME, 100);
	}

	D_MTOOneWay getUser() {
		return new D_MTOOneWay(USER_NAME);
	}
}