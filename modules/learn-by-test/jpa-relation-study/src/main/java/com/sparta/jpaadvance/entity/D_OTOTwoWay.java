package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_oto_twoway_dependent")
@NoArgsConstructor // 이거 없으면 오류난다 연관관계 때문인듯
public class D_OTOTwoWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToOne(mappedBy = "user")
    private OwnerOTOTwoWay food;

    public D_OTOTwoWay(String name){
        this.name = name;
    }

    public void addFood(OwnerOTOTwoWay food) {
        this.food = food;
        food.setUser(this);
    }
}