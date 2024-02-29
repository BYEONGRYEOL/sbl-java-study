package com.example.jpaadvancestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.jpaadvancestudy.repository.MyEntityRepository;

@DataJpaTest
public class TransactionalLearnTest {
	@Autowired
	private MyEntityRepository myEntityRepository;


}
