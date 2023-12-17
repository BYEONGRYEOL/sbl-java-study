package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.ManyToOne.TwoWay.UserMTOTwoWayDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMTOTwoWayDependentRepository extends JpaRepository<UserMTOTwoWayDependent, Long> {
}
