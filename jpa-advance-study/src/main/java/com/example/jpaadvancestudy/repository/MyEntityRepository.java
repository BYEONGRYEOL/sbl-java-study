package com.example.jpaadvancestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jpaadvancestudy.entity.MyEntity;

@Repository
public interface MyEntityRepository extends JpaRepository<MyEntity, Long> {

}
