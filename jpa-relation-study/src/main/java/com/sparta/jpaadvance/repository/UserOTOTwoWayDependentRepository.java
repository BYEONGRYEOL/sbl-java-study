package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToOne.TwoWay.UserOTOTwoWayDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOTOTwoWayDependentRepository extends JpaRepository<UserOTOTwoWayDependent, Long> {
}
