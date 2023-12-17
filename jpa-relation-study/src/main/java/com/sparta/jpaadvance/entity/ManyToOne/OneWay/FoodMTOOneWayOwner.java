package com.sparta.jpaadvance.entity.ManyToOne.OneWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "food_mto_oneway_owner")
@NoArgsConstructor
public class FoodMTOOneWayOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    @ManyToOne
    @JoinColumn(name = "user_mto_oneway_dependent_id")
    private UserMTOOneWayDependent user;

    public FoodMTOOneWayOwner(String foodName, double price) {
        this.name = foodName;
        this.price = price;
    }
}