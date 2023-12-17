package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToOne.TwoWay.FoodOTOTwoWayOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOTOTwoWayOwnerRepository extends JpaRepository<FoodOTOTwoWayOwner, Long> {
}
