package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "food_mto_oneway_owner")
@NoArgsConstructor
public class OwnerMTOOneWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    @ManyToOne
    @JoinColumn(name = "user_mto_oneway_dependent_id")
    private D_MTOOneWay user;

    public OwnerMTOOneWay(String foodName, double price) {
        this.name = foodName;
        this.price = price;
    }
}