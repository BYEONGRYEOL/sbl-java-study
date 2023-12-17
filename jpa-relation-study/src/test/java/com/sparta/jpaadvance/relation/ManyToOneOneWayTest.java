package com.sparta.jpaadvance.relation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.jpaadvance.entity.ManyToOne.OneWay.FoodMTOOneWayOwner;
import com.sparta.jpaadvance.entity.ManyToOne.OneWay.UserMTOOneWayDependent;
import com.sparta.jpaadvance.repository.FoodMTOOneWayOwnerRepository;
import com.sparta.jpaadvance.repository.UserMTOOneWayDependentRepository;

@Transactional
@DataJpaTest
public class ManyToOneOneWayTest {

	private final String FOOD_NAME = "foodName";
	private final String USER_NAME = "username";
	@Autowired
	UserMTOOneWayDependentRepository userRepository;
	@Autowired
	FoodMTOOneWayOwnerRepository foodRepository;

	@Test
	@Rollback(value = false)
	@DisplayName("N대1 단방향 테스트")
	void insertManyRelatedByOneTest() {
		UserMTOOneWayDependent user = new UserMTOOneWayDependent();
		user.setName("Robbie");

		FoodMTOOneWayOwner food = getFood();
		food.setUser(user); // 외래 키(연관 관계) 설정

		FoodMTOOneWayOwner food2 = getFood();
		food2.setUser(user); // 외래 키(연관 관계) 설정

		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);
	}

	FoodMTOOneWayOwner getFood() {
		return new FoodMTOOneWayOwner(FOOD_NAME, 100);
	}

	UserMTOOneWayDependent getUser() {
		return new UserMTOOneWayDependent(USER_NAME);
	}
}