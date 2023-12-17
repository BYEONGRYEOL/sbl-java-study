package com.example.jpaadvancestudy;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class NoGenerateIdEntity {

	@Id
	private Long id;
	private String name;

	public NoGenerateIdEntity(String name){
		this.name = name;
	}
}
