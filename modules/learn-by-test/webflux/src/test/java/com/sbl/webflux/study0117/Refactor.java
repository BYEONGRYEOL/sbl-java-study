package com.sbl.webflux.study0117;

import static com.sbl.webflux.Util.ReactorUtil.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sbl.webflux.Util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Refactor {

	/**
	 * nested chain이 발생하는 경우 반드시 그래야만하는지 한번 더 고민해보자
	 * nested chaining하지 않으면 mapper function 자리에 lambda 대신 method reference도 사용 가능하다.
	 */
	@DisplayName("리팩터1-1")
	@Test
	void notUseNestedFlatMap() {

		// bad code
		Mono<String> badMono = Util.ReactorUtil.integerMono()
			.flatMap(integer -> Util.ReactorUtil.stringMono(integer)
				.flatMap(Util.ReactorUtil::stringRepeatMono)
			);

		badMono.subscribe(log::info);

		// good code
		Mono<String> goodMono = Util.ReactorUtil.integerMono()
			.flatMap(Util.ReactorUtil::stringMono)
			.flatMap(Util.ReactorUtil::stringRepeatMono);

		goodMono.subscribe(log::info);

	}

	@DisplayName("리팩터1-2")
	@Test
	void filterWhen() {

		// 깔끔해보이긴 하지만 중첩 구조의 flatMap 을 사용중이다. 코드에 문제가 생겼을 때 건들기에 난이도가 높음
		// 심지어 중첩된 flatMap에서는 해당 스트림에서 요소로 들어있는 name을 사용하는 것이 아니라 상위 스트림의 id를 사용중이다.
		// 오해하기 좋은 코드이다.
		Flux<String> badFlux = Flux.just(-1, 0, 1)
			.flatMap(id -> getName(id)
				.filter(name -> !"john".equals(name))
				.flatMap(name -> toMono(id))
			);

		// 좋은 예, filterWhen -> map을 이용해서 복잡한 조건의 filter를 수행한 뒤 가공된 id로 flatMap을 수행하고 있음
		Flux<String> goodFlux = Flux.just(-1, 0, 1)
			.filterWhen(id -> getName(id).map(name -> !"john".equals(name)))
			.flatMap(Util.ReactorUtil::reactiveOperation);

		badFlux.subscribe(log::info);
		goodFlux.subscribe(log::info);
	}

	@DisplayName("리팩터1-3")
	@Test
	void filterWhen2() {
		// 무분별한 nested flatMap 사용이 아닌 filterWhen사용으로 가독성이 개선되었지만
		// filterWhen에 전달된 lambda function을 이해하는데 시간을 들여야 한다는 점이 걸린다.
		Flux<String> before = Flux.just(-1, 0, 1)
			.filterWhen(id -> getName(id).map(name -> !"john".equals(name)))
			.flatMap(Util.ReactorUtil::reactiveOperation);

		before.subscribe(log::info);

		Flux<String> after = Flux.just(-1, 0, 1)
			.filterWhen(this::isNotJohn)
			.flatMap(Util.ReactorUtil::reactiveOperation);

		after.subscribe(log::info);

	}

	private Mono<Boolean> isNotJohn(Integer id) {
		return getName(id).map(name -> !"john".equals(name)).onErrorReturn(false);
	}

	/**
	 * 주의** map은 flatMap과 달리 동기식으로 작동해야하기때문에
	 * 동기식으로 동작해도되는? or 동기식으로 동작해야하는 로직에만 map 사용
	 * Flux.flatMap(return Mono) 의 결과는 각각의 Mono publisher 들이 모인 Flux가 된다.
	 * 그럴거면 Flux.map(어떻게 바뀔지) 로 더 가독성 좋게 사용할 수 있음
	 */
	@Test
	void useMapInsteadOfReturningMonoInFlatMap() {
		//given
		Flux<String> badCase = Flux.just(-1, 0, 1)
			.flatMap(myNumber -> Mono.just("문자로 변환된 숫자" + myNumber));

		Flux<String> goodCase = Flux.just(-1, 0, 1)
			.map(myNumber -> "문자로 변환된 숫자" + myNumber);

		badCase.subscribe(i -> log.info(i.toString()));
		goodCase.subscribe(i -> log.info(i.toString()));
	}

}


