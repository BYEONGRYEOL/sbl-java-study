package com.sparta.jpaadvance.repository;

import com.sparta.jpaadvance.entity.OwnerOTOOneWay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Owner_OTOOneWayOwnerRepository extends JpaRepository<OwnerOTOOneWay, Long> {
    public Optional<OwnerOTOOneWay> findByName(String name);
}
