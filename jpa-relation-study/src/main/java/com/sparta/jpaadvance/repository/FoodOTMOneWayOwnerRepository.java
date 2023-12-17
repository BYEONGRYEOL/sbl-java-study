package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToMany.OneWay.FoodOTMOneWayOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOTMOneWayOwnerRepository extends JpaRepository<FoodOTMOneWayOwner, Long> {
}
