package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_oto_oneway_dependent")
@NoArgsConstructor
public class D_OTOOneWay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public D_OTOOneWay(String userName) {
        this.name = userName;
    }
}