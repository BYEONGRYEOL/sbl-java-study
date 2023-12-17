package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToOne.OneWay.UserOTOOneWayDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOTOOneWayDependentRepository extends JpaRepository<UserOTOOneWayDependent, Long> {
}
