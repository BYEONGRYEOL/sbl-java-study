package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.ManyToOne.TwoWay.FoodMTOTwoWayOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodMTOTwoWayOwnerRepository extends JpaRepository<FoodMTOTwoWayOwner, Long> {

}
