package com.sparta.jpaadvance.entity.OneToOne.OneWay;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_oto_oneway_dependent")
@NoArgsConstructor
public class UserOTOOneWayDependent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public UserOTOOneWayDependent(String userName) {
        this.name = userName;
    }
}