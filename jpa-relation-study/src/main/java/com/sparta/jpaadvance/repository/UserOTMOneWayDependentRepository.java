package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToMany.OneWay.UserOTMOneWayDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOTMOneWayDependentRepository extends JpaRepository<UserOTMOneWayDependent, Long> {
}
