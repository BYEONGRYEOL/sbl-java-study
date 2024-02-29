package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "food_otm_oneway_owner")
@NoArgsConstructor
public class OwnerOTMOneWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id") // users 테이블에 food_id 컬럼
    private List<D_OTMOneWay> userList = new ArrayList<>();

    public OwnerOTMOneWay(String foodName, double price) {
        this.name = foodName;
        this.price = price;
    }
}