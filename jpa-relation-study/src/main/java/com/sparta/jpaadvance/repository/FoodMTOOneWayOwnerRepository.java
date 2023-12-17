package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.ManyToOne.OneWay.FoodMTOOneWayOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodMTOOneWayOwnerRepository extends JpaRepository<FoodMTOOneWayOwner, Long> {
}
