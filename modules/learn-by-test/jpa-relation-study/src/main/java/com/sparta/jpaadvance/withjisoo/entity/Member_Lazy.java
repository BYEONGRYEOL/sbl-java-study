package com.sparta.jpaadvance.withjisoo.entity;

import java.util.ArrayList;
import java.util.List;

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
public class Member_Lazy {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_lazy_id")
	private Long memberId;
	private String name;

	@OneToMany(mappedBy = "memberLazy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Snack_Lazy> snackLazyList = new ArrayList<>();

	public Member_Lazy(String name) {
		this.name = name;
	}
}
