package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "food_oto_oneway_owner")
@NoArgsConstructor
public class OwnerOTOOneWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private double price;

    @OneToOne
    @JoinColumn(name = "user_oto_oneway_dependent_id")
    private D_OTOOneWay user;

    public OwnerOTOOneWay(String name, double price) {
        this.name = name;
        this.price = price;
    }
}

