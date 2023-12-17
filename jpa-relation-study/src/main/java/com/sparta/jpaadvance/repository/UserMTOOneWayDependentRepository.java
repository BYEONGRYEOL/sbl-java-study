package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.ManyToOne.OneWay.UserMTOOneWayDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMTOOneWayDependentRepository extends JpaRepository<UserMTOOneWayDependent, Long> {
}
