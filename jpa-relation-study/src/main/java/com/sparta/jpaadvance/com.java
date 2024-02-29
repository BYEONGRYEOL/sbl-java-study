package com.sparta.jpaadvance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sparta.jpaadvance.withjisoo.entity.Member_Lazy;
import com.sparta.jpaadvance.withjisoo.entity.Snack_Lazy;
import com.sparta.jpaadvance.withjisoo.repository.Member_LazyRepository;
import com.sparta.jpaadvance.withjisoo.repository.Snack_LazyRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class com {
	private final Member_LazyRepository memberLazyRepository;
	private final Snack_LazyRepository snackLazyRepository;
	@PostConstruct
	public void dbinsert(){
			int snackIndex = 1;
			for (int i = 1; i <= 10; i++) {
				Member_Lazy memberLazy = getMemberLazy("멤버" + i);
				List<Snack_Lazy> snackLazyList = new ArrayList<>();
				for (int j = 0; j < 5; j++) {
					Snack_Lazy snackLazy = getSnackLazy("과자" + snackIndex++);
					snackLazyList.add(snackLazy);
					snackLazy.setMemberLazy(memberLazy);
				}
				memberLazy.setSnackLazyList(snackLazyList);
				memberLazyRepository.save(memberLazy);
				snackLazyRepository.saveAll(snackLazyList);
			}
	}

	private Snack_Lazy getSnackLazy(String s) {
		return new Snack_Lazy(s);
	}

	private Member_Lazy getMemberLazy(String s) {
		return new Member_Lazy(s);
	}
}
