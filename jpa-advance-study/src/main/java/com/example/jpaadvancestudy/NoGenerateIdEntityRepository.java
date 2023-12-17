package com.example.jpaadvancestudy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoGenerateIdEntityRepository extends JpaRepository<NoGenerateIdEntity, Long> {

}
