package com.sparta.jpaadvance.relation;

import com.sparta.jpaadvance.entity.OneToOne.OneWay.FoodOTOOneWayOwner;
import com.sparta.jpaadvance.entity.OneToOne.OneWay.UserOTOOneWayDependent;
import com.sparta.jpaadvance.repository.FoodOTOOneWayOwnerRepository;
import com.sparta.jpaadvance.repository.UserOTOOneWayDependentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@DataJpaTest
public class OneToOneOneWayTest {

    private final String FOOD_NAME = "foodName";
    private final String USER_NAME = "username";

    @Autowired
    FoodOTOOneWayOwnerRepository foodRepository;
    @Autowired
    UserOTOOneWayDependentRepository userRepository;

//    @Test
//    void insertNullValueIntoNonNullColumnTest() {
//        FoodOTOOneWayOwner hasNullNameFood = new FoodOTOOneWayOwner();
//        hasNullNameFood.setPrice(1234);
//        assertThatThrownBy(()->foodRepository.save(hasNullNameFood))
//                .isInstanceOf(DataIntegrityViolationException.class)
//                .hasMessageContaining("not-null property references a null or transient value");
//    }


    @Test
    @Rollback(value = false) // 테스트에서는 @Transactional 에 의해 자동 rollback 됨으로 false 설정해준다.
    @DisplayName("1대1 단방향 외래키 저장 성공 테스트")
    void oneToOneForiegnKeySuccessTest() {

        UserOTOOneWayDependent user = getUser();
        FoodOTOOneWayOwner food = getFood();

        // 외래 키의 주인인 Food Entity user 필드에 user 객체를 추가해 줍니다.
        food.setUser(user); // 외래 키(연관 관계) 설정

        // 어차피 save에는 Transactional 어노테이션이 달려있다.
        userRepository.save(user);
        foodRepository.save(food);
    }

    @Test
    @Rollback(value = false) // 테스트에서는 @Transactional 에 의해 자동 rollback 됨으로 false 설정해준다.
    @DisplayName("1대1 단방향 외래키 저장 실패 테스트")
    void oneToOneForiegnKeyFailTest() {

        UserOTOOneWayDependent user = getUser();

        // 외래 키의 주인인 Food Entity user 필드에 user 객체를 추가해 줍니다.
        FoodOTOOneWayOwner food = getFood();

        // 어차피 save에는 Transactional 어노테이션이 달려있다.
        userRepository.save(user);
        foodRepository.save(food);
    }



    FoodOTOOneWayOwner getFood() {
        return new FoodOTOOneWayOwner(FOOD_NAME, 100);
    }
    UserOTOOneWayDependent getUser(){
        return new UserOTOOneWayDependent(USER_NAME);
    }
}

