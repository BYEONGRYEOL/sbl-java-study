package com.sparta.jpaadvance.withjisoo.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Member_Eager {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_eager_id")
	private Long memberId;
	private String name;


	@OneToMany(mappedBy = "memberEager", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Snack_Eager> snackEagerList = new ArrayList<>();

	public Member_Eager(String name) {
		this.name = name;
	}
}
