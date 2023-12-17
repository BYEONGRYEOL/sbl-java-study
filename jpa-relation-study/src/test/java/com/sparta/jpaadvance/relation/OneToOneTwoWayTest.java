package com.sparta.jpaadvance.relation;

import com.sparta.jpaadvance.entity.OneToOne.TwoWay.FoodOTOTwoWayOwner;
import com.sparta.jpaadvance.entity.OneToOne.TwoWay.UserOTOTwoWayDependent;
import com.sparta.jpaadvance.repository.FoodOTOTwoWayOwnerRepository;
import com.sparta.jpaadvance.repository.UserOTOTwoWayDependentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@DataJpaTest
public class OneToOneTwoWayTest {

    private final String FOOD_NAME = "foodName";
    private final String USER_NAME = "username";

    @Autowired
    FoodOTOTwoWayOwnerRepository foodRepository;
    @Autowired
    UserOTOTwoWayDependentRepository userRepository;

    @Test
    @Rollback(value = false)
    @DisplayName("1대1 양방향 테스트 : 주인이 아닌 엔티티에서 저장시 외래키 저장 안됨")
    void savedByDependentOnlyTest() {
        FoodOTOTwoWayOwner food = getFood();

        // 외래 키의 주인이 아닌 User 에서 Food 를 저장해보겠습니다.
        UserOTOTwoWayDependent user = getUser();
        user.setFood(food);

        userRepository.save(user);
        foodRepository.save(food);
        // 확인해 보시면 user_id 값이 들어가 있지 않은 것을 확인하실 수 있습니다.

    }

    @Test
    @Rollback(value = false)
    @DisplayName("1대1 양방향 테스트 : 외래 키성공")
    void savedByDependentAndOwnerTest() {
        FoodOTOTwoWayOwner food = getFood();

        UserOTOTwoWayDependent user = getUser();
        // 직접 작성한 addFood 함수
        user.addFood(food);

        userRepository.save(user);
        foodRepository.save(food);
    }

    @Test
    @Rollback(value = false)
    @DisplayName("1대1 양방향 테스트 주인이 dependent를 저장")
    void savedByOwnerTest() {
        FoodOTOTwoWayOwner food = getFood();
        UserOTOTwoWayDependent user = getUser();

        food.setUser(user); // 외래 키(연관 관계) 설정

        userRepository.save(user);
        foodRepository.save(food);
    }


    // TODO: 2023-11-10 조회 테스트 수정 필요
    @Test
    @DisplayName("1대1 조회 : Owner 기준 Dependent 정보 조회")
    void findDependentByOwnerTest() {
        FoodOTOTwoWayOwner food = foodRepository.findById(1L).orElseThrow(NullPointerException::new);
        // 음식 정보 조회
        System.out.println("food.getName() = " + food.getName());

        // 음식을 주문한 고객 정보 조회
        System.out.println("food.getUser().getName() = " + food.getUser().getName());
    }

    @Test
    @DisplayName("1대1 조회 : dependent 기준 owner 정보 조회")
    void findOwnerByDependentTest() {
        UserOTOTwoWayDependent user = userRepository.findById(1L).orElseThrow(NullPointerException::new);
        // 고객 정보 조회
        System.out.println("user.getName() = " + user.getName());

        // 해당 고객이 주문한 음식 정보 조회
        FoodOTOTwoWayOwner food = user.getFood();
        System.out.println("food.getName() = " + food.getName());
        System.out.println("food.getPrice() = " + food.getPrice());
    }
    FoodOTOTwoWayOwner getFood() {
        return new FoodOTOTwoWayOwner(FOOD_NAME, 100);
    }
    UserOTOTwoWayDependent getUser(){
        return new UserOTOTwoWayDependent(USER_NAME);
    }
}
