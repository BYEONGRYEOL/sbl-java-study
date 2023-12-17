package com.sparta.jpaadvance.entity.ManyToOne.TwoWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "user_mto_twoway_dependent")
@NoArgsConstructor
public class UserMTOTwoWayDependent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "user")
    private List<FoodMTOTwoWayOwner> foodList = new ArrayList<>();

    public UserMTOTwoWayDependent(String userName) { // 이걸 선언하는 순간 NoArgsConstructor가 필요하다고 한다.
        this.name = userName;
    }

    public void addFoodList(FoodMTOTwoWayOwner food) {
        this.foodList.add(food);
        food.setUser(this); // 외래 키(연관 관계) 설정
    }
}