package com.example.jpaadvancestudy;

import static org.assertj.core.api.Assertions.*;

import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.jpa.JpaSystemException;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class HibernateTest {

	@Autowired
	private NoGenerateIdEntityRepository noGenerateIdEntityRepository;

	@Autowired
	private MyEntityRepository myEntityRepository;

	@Autowired
	private EntityManager em;

	/**
	 * 테이블의 기본키인 @Id 어노테이션이 붙은 필드에 @GeneratedValue 어노테이션이 붙어있지 않아서
	 * GenerationType을 지정하지 않은 상태의 엔티티를 save할 때의 오류
	 */
	@Test
	void saveFailedByIdentifierGenerationException() {
		// given
		String name = "test";
		NoGenerateIdEntity noGenerateIdEntity = new NoGenerateIdEntity(name);

		// when then
		assertThatThrownBy(() -> {
			noGenerateIdEntityRepository.save(noGenerateIdEntity);
		}).isInstanceOf(JpaSystemException.class)
			.hasMessageContaining(
				"must be manually assigned before calling 'persist()'")
			.hasCauseInstanceOf(IdentifierGenerationException.class);
	}

	/**
	 * 테이블의 기본키인 @Id 어노테이션이 붙은 필드에 @GeneratedValue(GenerationType.Identity) 어노테이션이 붙은 경우
	 * JpaRepository를 통해 save할때 기본키의 Id가 자동으로 생성된다.
	 */
	@Test
	void saveSuccessWithIdGeneration(){
	    // given
		String name = "test";
		MyEntity myEntity = new MyEntity(name);

		// when
		myEntityRepository.save(myEntity);
		MyEntity myEntityInEntityManager = em.find(MyEntity.class, 1);

	    // then
		assertThat(myEntity).isEqualTo(myEntityInEntityManager);
	}

	@Test
	void saveFailedBy(){
	    // given
		MyEntity myEntity = getMyEntity();

	    // when

	    // then

	}

	private MyEntity getMyEntity(){
		return new MyEntity("test");
	}

}
