package com.sparta.jpaadvance.entity.ManyToOne.TwoWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "food_mto_twoway_owner")
@NoArgsConstructor
public class FoodMTOTwoWayOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    @ManyToOne
    @JoinColumn(name = "user_mto_twoway_dependent_id")
    private UserMTOTwoWayDependent user;

    public FoodMTOTwoWayOwner(String foodName, double price) {
        this.name = foodName;
        this.price = price;
    }
}