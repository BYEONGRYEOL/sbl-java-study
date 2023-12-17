package com.sparta.jpaadvance.entity.OneToMany.OneWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "user_otm_oneway_dependent")
@NoArgsConstructor
public class UserOTMOneWayDependent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public UserOTMOneWayDependent(String userName) {
        this.name = userName;
    }
}