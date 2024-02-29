package com.example.jpaadvancestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jpaadvancestudy.entity.NoGenerateIdEntity;

@Repository
public interface NoGenerateIdEntityRepository extends JpaRepository<NoGenerateIdEntity, Long> {

}
