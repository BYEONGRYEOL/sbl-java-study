package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OneToOne.OneWay.FoodOTOOneWayOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FoodOTOOneWayOwnerRepository extends JpaRepository<FoodOTOOneWayOwner, Long> {
    public Optional<FoodOTOOneWayOwner> findByName(String name);
}
