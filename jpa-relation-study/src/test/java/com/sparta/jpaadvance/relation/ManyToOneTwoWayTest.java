package com.sparta.jpaadvance.relation;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.jpaadvance.entity.ManyToOne.TwoWay.FoodMTOTwoWayOwner;
import com.sparta.jpaadvance.entity.ManyToOne.TwoWay.UserMTOTwoWayDependent;
import com.sparta.jpaadvance.repository.FoodMTOTwoWayOwnerRepository;
import com.sparta.jpaadvance.repository.UserMTOTwoWayDependentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Transactional
@DataJpaTest
public class ManyToOneTwoWayTest {

	@Autowired
	UserMTOTwoWayDependentRepository userRepository;
	@Autowired
	FoodMTOTwoWayOwnerRepository foodRepository;
	@PersistenceContext
	private EntityManager em;

	@Test
	@Rollback
	@DisplayName("N대1 양방향 테스트 : 외래 키 저장 실패")
	void test2() {

		FoodMTOTwoWayOwner food = getFood("food1", 100);
		FoodMTOTwoWayOwner food2 = getFood("food2", 200);

		// 외래 키의 주인이 아닌 User 에서 Food 를 저장해보겠습니다.
		UserMTOTwoWayDependent user = getUser("user");

		user.getFoodList().add(food);
		user.getFoodList().add(food2);

		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);

		// 확인해 보시면 user_id 값이 들어가 있지 않은 것을 확인하실 수 있습니다.
	}

	@Test
	@Rollback
	@DisplayName("N대1 양방향 테스트 : 외래 키 저장 실패 -> 성공")
	void test3() {

		FoodMTOTwoWayOwner food = getFood("food1", 100);
		FoodMTOTwoWayOwner food2 = getFood("food2", 200);

		// 외래 키의 주인이 아닌 User 에서 Food 를 쉽게 저장하기 위해 addFoodList() 메서드 생성하고
		// 해당 메서드에 외래 키(연관 관계) 설정 food.setUser(this); 추가
		UserMTOTwoWayDependent user = getUser("user");
		user.addFoodList(food);
		user.addFoodList(food2);

		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);
	}

	@Test
	@Rollback
	@DisplayName("N대1 양방향 테스트")
	void test4() {
		FoodMTOTwoWayOwner food = getFood("food1", 100);
		FoodMTOTwoWayOwner food2 = getFood("food2", 200);

		// 외래 키의 주인이 아닌 User 에서 Food 를 저장해보겠습니다.
		UserMTOTwoWayDependent user = getUser("user");

		food.setUser(user); // 외래 키(연관 관계) 설정
		food2.setUser(user); // 외래 키(연관 관계) 설정

		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);
	}

	@Test
	@DisplayName("N대1 조회 : Food 기준 user 정보 조회")
	@Transactional
	@Rollback
	void test5() {
		FoodMTOTwoWayOwner food = getFood("food1", 100);
		FoodMTOTwoWayOwner food2 = getFood("food2", 200);

		// 외래 키의 주인이 아닌 User 에서 Food 를 저장해보겠습니다.
		UserMTOTwoWayDependent user = getUser("user");

		food.setUser(user); // 외래 키(연관 관계) 설정
		food2.setUser(user); // 외래 키(연관 관계) 설정
		userRepository.save(user);
		foodRepository.save(food);
		foodRepository.save(food2);

		flush();

		FoodMTOTwoWayOwner findFood = foodRepository.findById(food.getId()).orElseThrow(NullPointerException::new);
		// 음식 정보 조회
		System.out.println("food.getName() = " + findFood.getName());

		// 음식을 주문한 고객 정보 조회
		System.out.println("food.getUser().getName() = " + findFood.getUser().getName());
	}

	@Test
	@Transactional
	@Rollback
	@DisplayName("N대1 조회 : User 기준 food 정보 조회")
	void test6() {

		FoodMTOTwoWayOwner food = getFood("food1", 100);
		FoodMTOTwoWayOwner food2 = getFood("food2", 200);

		// 외래 키의 주인이 아닌 User 에서 Food 를 저장해보겠습니다.
		UserMTOTwoWayDependent user = getUser("user");

		food.setUser(user); // 외래 키(연관 관계) 설정
		food2.setUser(user); // 외래 키(연관 관계) 설정

		foodRepository.save(food);
		foodRepository.save(food2);
		userRepository.save(user);

		flush();

		UserMTOTwoWayDependent findUser = userRepository.findById(user.getId()).orElseThrow(NullPointerException::new);
		// 고객 정보 조회
		System.out.println("findUser.getName() = " + findUser.getName());

		// 해당 고객이 주문한 음식 정보 조회
		List<FoodMTOTwoWayOwner> foodList = findUser.getFoodList();
		for (FoodMTOTwoWayOwner userFood : foodList) {
			System.out.println("userFood.getName() = " + userFood.getName());
			System.out.println("userFood.getPrice() = " + userFood.getPrice());
		}
	}

	FoodMTOTwoWayOwner getFood(String foodName, int price) {
		return new FoodMTOTwoWayOwner(foodName, price);
	}

	UserMTOTwoWayDependent getUser(String username) {
		return new UserMTOTwoWayDependent(username);
	}

	private void flush() {
		em.flush();
		em.clear();
	}
}