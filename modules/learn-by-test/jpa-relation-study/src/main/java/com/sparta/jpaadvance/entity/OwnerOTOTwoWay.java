package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "food_owo_twoway_owner")
@NoArgsConstructor
public class OwnerOTOTwoWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private double price;

    @OneToOne
    @JoinColumn(name = "user_oto_twoway_dependent_id")
    private D_OTOTwoWay user;

    public OwnerOTOTwoWay(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public void setUser(D_OTOTwoWay user) {
        this.user = user;
    }
}

