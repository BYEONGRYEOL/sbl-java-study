package com.sparta.jpaadvance.entity.ManyToOne.OneWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "user_mto_oneway_dependent")
@NoArgsConstructor
public class UserMTOOneWayDependent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public UserMTOOneWayDependent(String userName) {
        this.name = name;
    }
}