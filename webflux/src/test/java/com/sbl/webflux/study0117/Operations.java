package com.sbl.webflux.study0117;

import static com.sbl.webflux.Util.*;
import static com.sbl.webflux.Util.ReactorUtil.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.test.StepVerifier;

@Slf4j
public class Operations {

	/**
	 * `object_service()` streams sequence of Objects, but if you peek into service implementation, you can see
	 * that these items are in fact strings!
	 * Casting using `map()` to cast is one way to do it, but there is more convenient way.
	 * Remove `map` operator and use more appropriate operator to cast sequence to String.
	 */
	@Test
	public void cast() {

		Flux<String> badcase = object_service().flatMap(i -> Mono.just(i.toString()));

		Flux<String> goodcase = object_service().cast(String.class);

		StepVerifier.create(badcase).expectNext("1", "2", "3", "4", "5").verifyComplete();

		StepVerifier.create(goodcase).expectNext("1", "2", "3", "4", "5").verifyComplete();
	}

	/**
	 * `maybe_service()` may return some result.
	 * In case it doesn't return any result, return value "no results".
	 */
	@Test
	public void maybe() {
		Mono<String> result = maybe_service().defaultIfEmpty("no results");

		StepVerifier.create(result).expectNext("no results").verifyComplete();
	}

	/**
	 * Reduce the values from `numerical_service()` into a single number that is equal to sum of all numbers emitted by
	 * this service.
	 * <p>
	 * 	<img src="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/reduceWithSameReturnType.svg" alt="" style="background-color: #e0e0e0;" >
	 * 	<p>
	 */
	@Test
	public void sequence_sum() {
		//todo: change code as you need
		Mono<Integer> explicitBifunction = numerical_service().reduce(
			(beforeValue, afterValue) -> beforeValue + afterValue);

		Mono<Integer> methodReference = numerical_service().reduce(Integer::sum);

		//don't change code below
		StepVerifier.create(explicitBifunction).expectNext(55).verifyComplete();
		StepVerifier.create(methodReference).expectNext(55).verifyComplete();
	}

	/***
	 *  Reduce the values from `numerical_service()` but emit each intermediary number
	 *  Use first Flux value as initial value.
	 */
	@Test
	public void sum_each_successive() {
		Flux<Integer> sumEach = numerical_service().scan((before, after) -> before + after);

		StepVerifier.create(sumEach).expectNext(1, 3, 6, 10, 15, 21, 28, 36, 45, 55).verifyComplete();
	}

	/**
	 * A developer who wrote `numerical_service()` forgot that sequence should start with zero, so you must prepend zero
	 * to result sequence.
	 *
	 * Do not alter `numerical_service` implementation!
	 * Use only one operator.
	 */
	@Test
	public void sequence_starts_with_zero() {
		Flux<Integer> result = numerical_service().startWith(0);

		StepVerifier.create(result).expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
	}

	public Flux<Object> mashed_data_service() {
		return Flux.just("1", new LinkedList<String>(), new AtomicReference<String>(), "String.class", String.class);
	}

	@Test
	public void needle_in_a_haystack() {
		Flux<String> strings = mashed_data_service().ofType(String.class)
			//todo: change this line only
			;

		StepVerifier.create(strings).expectNext("1", "String.class").verifyComplete();
	}

	public Flux<Integer> room_temperature_service() {
		return Flux.interval(Duration.ofMillis(100), Duration.ofMillis(100))
			.take(20)
			.map(i -> ThreadLocalRandom.current().nextInt(10, 30));
	}

	/**
	 * doOnNext는 모든 emit되는 모든 요소에 대해 side effect 적용
	 */
	@Test
	public void atomic_counter() {
		AtomicInteger counter = new AtomicInteger(0);

		Flux<Integer> temperatureFlux = room_temperature_service().doOnNext(value -> {
			System.out.println(value);
			counter.getAndIncrement();
		});

		StepVerifier.create(temperatureFlux).expectNextCount(20).verifyComplete();

		Assertions.assertEquals(counter.get(), 20);
	}

	/**
	 * doOnComplete는 오류 없이 종료되었을 때만
	 */
	@Test
	public void successfully_executed() {
		AtomicInteger hooksTriggeredCounter = new AtomicInteger(0);

		Flux<Integer> temperatureFlux = room_temperature_service().doOnComplete(hooksTriggeredCounter::getAndIncrement);
		Flux<String> errorFlux = errorFlux().doOnComplete(hooksTriggeredCounter::getAndIncrement);

		// 취소된 경우 onComplete가 실행되지 않는다.
		StepVerifier.create(temperatureFlux.take(0)).expectNextCount(0).verifyComplete();
		Assertions.assertEquals(hooksTriggeredCounter.get(), 0);

		// skip 20 해서 다음 요소는 없지만 종료 -> 빈 Flux로 종료 -> onComplete가 실행된다.
		StepVerifier.create(temperatureFlux.skip(20)).expectNextCount(0).verifyComplete();
		Assertions.assertEquals(hooksTriggeredCounter.get(), 1);

		// 예외 발생시 onComplete가 실행되지 않는다.
		StepVerifier.create(errorFlux).expectError().verify();
		Assertions.assertEquals(hooksTriggeredCounter.get(), 1);
	}

	/**
	 * doOnTerminate는 성공적으로 complete, error로 인해 실패하는 경우를 감지
	 * cancel된 경우 감지하지 못한다.
	 * by completing successfully or failing with an error.
	 */
	@Test
	public void terminator() {
		AtomicInteger hooksTriggeredCounter = new AtomicInteger(0);

		Flux<Integer> temperatureFlux = room_temperature_service().doOnTerminate(
			hooksTriggeredCounter::getAndIncrement);

		// 취소 -> 종료되지 않음
		StepVerifier.create(temperatureFlux.take(0)).expectNextCount(0).verifyComplete();

		// skip 20 해서 다음 요소는 없지만 종료 -> 빈 Flux로 종료
		StepVerifier.create(temperatureFlux.skip(20)).expectNextCount(0).verifyComplete();

		// 예외 발생시에도 doOnTerminate는 실행
		StepVerifier.create(temperatureFlux.skip(20).concatWith(Flux.error(new RuntimeException("oops"))))
			.expectError()
			.verify();

		Assertions.assertEquals(hooksTriggeredCounter.get(), 2);
	}

	/**
	 * doOnCancel은 끝난 경우에만 종료
	 */
	@Test
	public void need_to_cancel() {
		AtomicBoolean canceled = new AtomicBoolean(false);
		AtomicInteger hooksTriggeredCounter = new AtomicInteger(0);

		Flux<Integer> temperatureFlux = room_temperature_service().doOnCancel(() -> canceled.set(true));

		// 취소 -> 종료되지 않음
		StepVerifier.create(temperatureFlux.take(0)).expectNextCount(0).verifyComplete();

		// skip 20 해서 다음 요소는 없지만 종료 -> 빈 Flux로 종료
		StepVerifier.create(temperatureFlux.skip(20)).expectNextCount(0).verifyComplete();

		// 예외 발생시에도 doOnTerminate는 실행
		StepVerifier.create(temperatureFlux.skip(20).concatWith(Flux.error(new RuntimeException("oops"))))
			.expectError()
			.verify();

		Assertions.assertEquals(hooksTriggeredCounter.get(), 1);
		Assertions.assertTrue(canceled.get());
	}

	/**
	 * Add a side effect that increments `hooksTriggeredCounter` when the `temperatureFlux` terminates, either when
	 * completing successfully, gets canceled or failing with an error.
	 * Use only one operator!
	 */
	@Test
	public void one_to_catch_them_all() {
		AtomicInteger hooksTriggeredCounter = new AtomicInteger(0);

		Flux<Integer> temperatureFlux = room_temperature_service().doFinally(
			ended -> hooksTriggeredCounter.getAndIncrement());

		StepVerifier.create(temperatureFlux.take(0)).expectNextCount(0).verifyComplete();

		StepVerifier.create(temperatureFlux.skip(20)).expectNextCount(0).verifyComplete();

		StepVerifier.create(temperatureFlux.skip(20).concatWith(Flux.error(new RuntimeException("oops"))))
			.expectError()
			.verify();

		Assertions.assertEquals(hooksTriggeredCounter.get(), 3);
	}

	// 위처럼 filter를 사용하는 경우 Empty Publisher가 되었을 가능성이 항상 0이 될 순 없기 때문에,
	// 비즈니스로직이 수행되지 않는 상황을 피하려면 switchIfEmpty나 defaultIfEmpty를 사용해야함

	// switchIfEmpty의 경우, filter 등에 의해 여태까지의 publisher chain 이 empty publisher인 경우
	// swithchIfEmpty에서 리턴하는 요소가 추가된다.

	/**
	 * 가장 위의 Mono가 구독되기 전에 실행한다.
	 * 그러므로 reactive streams에서 구독되는 순서로 실행됨,
	 */
	@Test
	public void ordering_is_important() {
		CopyOnWriteArrayList<String> sideEffects = new CopyOnWriteArrayList<>();

		Mono<Boolean> just = Mono.just(true)
			.doFirst(() -> sideEffects.add("three"))
			.doFirst(() -> sideEffects.add("two"))
			.doFirst(() -> sideEffects.add("one"));

		List<String> orderOfExecution = Arrays.asList("one", "two", "three"); //todo: change this line only

		StepVerifier.create(just).expectNext(true).verifyComplete();

		Assertions.assertEquals(sideEffects, orderOfExecution);
	}

	/**
	 * Following example is just a basic usage of `generate,`create`,`push` sinks. We will learn how to use them in a
	 * more complex scenarios when we tackle backpressure.
	 *
	 * Answer:
	 * - What is difference between `generate` and `create`?
	 * - What is difference between `create` and `push`?
	 */
	@Test
	public void generate_programmatically() {

		// SynchronousSink를 사용하여 동기적으로 생성한다.
		Flux<Integer> generateFlux = Flux.generate(() -> 0, (state, sink) -> {
			log.info(sink.toString());
			sink.next(state);
			if (state < 5) {
				return state + 1;
			} else {
				sink.complete();
				return state;
			}
		});

		// FluxSink를 사용하며 멀티스레드 기반으로 생성하는 경우에는 sink.complete()를 실행하지않아도 완료된다.
		Flux<Integer> createFlux = Flux.create(sink -> {
			log.info(sink.toString());
			for (int i = 0; i <= 5; i++) {
				sink.next(i);
			}
			sink.complete();
		});

		// Flux<Integer> createFluxMultiThread = Flux.create(sink -> {
		// 	log.info(sink.toString());
		// 	for (int i = 0; i <= 5; i++) {
		// 		int finalI = i;
		// 		new Thread(() -> sink.next(finalI)).start();
		// 	}
		// });

		// FluxSink를 사용하나 싱글스레드 Create mode : pushonly
		Flux<Integer> pushFlux = Flux.push(sink -> {
			log.info(sink.toString());
			for (int i = 0; i <= 5; i++) {
				sink.next(i);
			}
			sink.complete();
		});

		StepVerifier.create(generateFlux).expectNext(0, 1, 2, 3, 4, 5).verifyComplete();

		StepVerifier.create(createFlux).expectNext(0, 1, 2, 3, 4, 5).verifyComplete();
		// StepVerifier.create(createFluxMultiThread).expectNext(0, 1, 2, 3, 4, 5).verifyComplete();

		StepVerifier.create(pushFlux).expectNext(0, 1, 2, 3, 4, 5).verifyComplete();
	}

	/**
	 * 두 Flux의 결과가 순차적으로 실행되어서 연결되도록하는 방법
	 * Bonus: There are two ways to do this, check out both!
	 */
	@Test
	public void i_am_rubber_you_are_glue() {

		Flux<Integer> numbers = numberService1().concatWith(numberService2());
		;

		//don't change below this line
		StepVerifier.create(numbers).expectNext(1, 2, 3, 4, 5, 6, 7).verifyComplete();
	}

	public Flux<Integer> numberService1() {
		return Flux.range(1, 3).doOnNext(System.out::println);
	}

	public Flux<Integer> numberService2() {
		return Flux.range(4, 4).doOnNext(System.out::println);
	}

	@Test
	void switchIfEmpty() {
		Flux.just(-1, 0, 1)
			.filter(num -> num > 2)
			.flatMap(num -> Flux.just(String.valueOf(num)))
			.switchIfEmpty(Mono.just("switchIfEmpty"))
			.subscribe(log::info);
	}

	/**
	 * 두 Mono 사이 종속성이 없는 경우 간단하게 Mono.zip()을 활용하여
	 * 두 Mono의 값으로 비즈니스 로직을 수행할 수 있다. Mono<Tuple<M1,M2>> 를 반환한다.
	 */
	@Test
	void monoZipAndFlatMap() {
		// given
		Mono<String> loginMono = Mono.zip(getKey(), getPassword())
			.flatMap(tuple -> login(tuple.getT1(), tuple.getT2()));

		StepVerifier.create(loginMono).expectNext("jwt토큰").verifyComplete();
	}

	/**
	 * zip은 두 publisher의 반환값을 사용하기위한 operator지만 사용하지 않아도 되긴 함
	 */
	@Test
	void monoZip() {
		Mono.zip(successfulJob(), errorMono())
			// .log()
			.doOnSuccess((success) -> log.info("성공" + success))
			.doOnError((error) -> log.error("실패" + error))
			.subscribe();
	}

	/**
	 * 두 Mono의 반환값을 사용해야하는 경우 zip, 두 Mono의 반환값을 사용하지 않아도 되는 경우 when을 사용하는 것이 좋다.
	 */
	@Test
	void monoWhen() {
		Mono.when(successfulJob(), errorMono())
			// .log()
			.doOnSuccess((success) -> log.info("성공" + success))
			.doOnError((error) -> log.error("실패" + error))
			.subscribe();
	}

	/**
	 * 두 Mono 사이 종속성이 존재하는 경우 zipWhen()을 사용할 수 잇다.
	 * 직관적으로 두 Mono의 값을 가져오는 zip(A,B) 메서드와 달리 A.zipWhen(B) 와 같이 사용된다.
	 * A.zipWhen(B) 에서 A가 Publisher 역할, B가 subscriber역할을 하고있다고 생각하면 편함,
	 * A가 값을 방출한 후 방출된 요소로 B를 생성한다.
	 * publisher 역할의 Mono의 값으로 subscriber 역할의 Mono의 값을 생성하여 이후 Mono<Tuple<P,S>> 를 반환한다.
	 */
	@Test
	void zipWhen() {
		// badCase
		getPassword().flatMap(password -> {
			String encryptedPassword = encryptPassword(password);
			return login(password, encryptedPassword);
		}).subscribe(log::info);

		// goodCase
		getPassword().zipWhen(ReactorUtil::encryptPasswordMono)
			.flatMap(tuple -> login(tuple.getT1(), tuple.getT2()))
			.subscribe(log::info);
	}

	/**
	 * zip() 이든 zipWhen()이든 사용하다보면, tuple을 완성하여 생성하고, 요청해서 비즈니스 로직에서 tuple.getTN() 을 이용해서 튜플을 사용할 때
	 * 조금 사용하기 곤란함을 느꼈을 것이다.
	 * TupleUtils.function() 을 사용하면 Tuple에서 요소들을 꺼내서 바로 사용할 수 있도록 해준다.
	 */

	@Test
	void tupleUtils_function() {
		Mono<String> loginMono = Mono.zip(getKey(), getPassword()).flatMap(TupleUtils.function(ReactorUtil::login));
		StepVerifier.create(loginMono).expectNext("jwt토큰").verifyComplete();
	}

	/**
	 * Mono를 기준으로 Flux를 결합하기위해 flatMapMany 사용
	 */
	@Test
	void combineMonoAndFluxByFlatMapMany() {
		Mono<String> mono = Mono.just("서병렬");
		Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);

		Flux<String> combinedFlux = mono.flatMapMany(monoValue -> flux.map(fluxValue -> monoValue + fluxValue));

		combinedFlux.subscribe(log::info);
	}

	/**
	 * Flux 기준으로 Mono를 결합하기위해 flatMap 사용
	 */
	@Test
	void combineFluxWithMono() {
		Mono<String> mono = Mono.just("서병렬");
		Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);

		Flux<String> combinedFlux = flux.flatMap(fluxValue -> mono.map(monoValue -> monoValue + fluxValue));

		combinedFlux.subscribe(log::info);
	}

	/**
	 * Flux.zip 연산자로 flux와 mono 두 publisher를 동시에 구독하게되면
	 * mono가 가진 개수만큼 방출된다.
	 */
	@Test
	void combineFluxWithMonoByZip() {
		Mono<String> mono = Mono.just("서병렬");
		Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);

		Flux.zip(flux, mono).flatMap(TupleUtils.function(this::combineStringAndInteger)).subscribe(log::info);
	}

	private Mono<String> combineStringAndInteger(Integer i, String s) {
		return Mono.just(s + i);
	}

	/**
	 * Mono.defer는 Hot Publisher가 아닌 Cold Publisher이다.
	 * 따라서 SwitchIfEmpty 등 값을 방출하기 전에는 실행 될 필요가 없는 부분에 사용하면 좋다.
	 */
	@Test
	void deferChangesHotToCold() {

		System.out.println("\n\n\n\n");

		log.info("CASE::1 Mono.defer 사용 X switchIfEmpty가 동작함");
		Mono.just("\n\nCASE::1 종료\n\n")
			.filter(this::failedToPass)
			.switchIfEmpty(Mono.error(new MyError()))
			.subscribe(log::info);

		System.out.println("\n\n\n\n");

		log.info("CASE::2 Mono.defer 사용 X switchIfEmpty가 동작하지 않음");
		Mono.just("\n\nCASE::2 종료\n\n")
			.filter(this::passThrough)
			.switchIfEmpty(Mono.error(new MyError()))
			.subscribe(log::info);

		System.out.println("\n\n\n\n");

		log.info("CASE::3 Mono.defer 사용 O switchIfEmpty가 동작함");
		Mono.just("\n\nCASE::3 종료\n\n")
			.filter(this::failedToPass)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new MyError())))
			.subscribe(log::info);

		System.out.println("\n\n\n\n");

		log.info("CASE::4 Mono.defer 사용 O switchIfEmpty가 동작하지 않음");
		Mono.just("\n\nCASE::4 종료\n\n")
			.filter(this::passThrough)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new MyError())))
			.subscribe(log::info);

		System.out.println("\n\n\n\n");

		String dynamicString1 = UUID.randomUUID().toString();
		String dynamicString2 = UUID.randomUUID().toString();

		DynamicError dynamicError1;
		DynamicError dynamicError2;

		log.info("CASE::5 Mono.defer 사용 X 동적 문자열로 에러 객체 생성");
		Mono.just("\n\nCASE::5 종료\n\n")
			.filter(this::failedToPass)
			.switchIfEmpty(Mono.error(new DynamicError(dynamicString1)))
			.subscribe(log::error);

		System.out.println("\n\n\n\n");

		log.info("CASE::6 Mono.defer 사용 O 동적 문자열로 에러 객체 생성");
		Mono.just("\n\nCASE::6 종료\n\n")
			.filter(this::failedToPass)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new DynamicError(dynamicString2))))
			.subscribe(log::error);

	}

	@Test
	void pubsublearn() {
		Flux<Integer> publisher = Flux.just(1, 2, 3, 4).filter(i -> i % 2 == 0);

		BaseSubscriber<Integer> subscriber = new BaseSubscriber<>() {
			@Override
			protected void hookOnNext(Integer value) {
				System.out.println("Received: " + value);
			}
		};

		// 구독관계 생성
		publisher.subscribe(subscriber);
		// @4 주로 사용되는 subscribe 패턴
		publisher.subscribe(i -> System.out.println("Received: " + i));

		// @5 하나의 연결된 파이프라인으로 표현
		Flux.just(1, 2, 3, 4).filter(i -> i % 2 == 0).subscribe(i -> System.out.println("Received: " + i));

		Flux.just(1, 2, 3, 4, 5, 100, 1000)
			// 위의 just는 아래 filter의 Upstream이다.
			.filter(i -> i >= 100)
			// 아래 map은 위 filter의 Downstream이다.
			.map(String::valueOf).subscribe(System.out::println);
	}

	@Test
	void dispose() throws InterruptedException {

		List<Pen> penList = new ArrayList<>();

		penList.add(new Pen("검정"));
		penList.add(new Pen("불량"));
		penList.add(new Pen("빨강"));
		penList.add(new Pen("불량"));
		penList.add(new Pen("초록"));

		Flux<Pen> penWholesaler = Flux.fromIterable(penList)
			.delayElements(Duration.ofSeconds(1))
			.filter(Pen::checkQuality);

		Disposable contract = penWholesaler.subscribe(
			pen -> System.out.printf("도매상에게서 %s을(를) 100원에 샀다.%n손님에게 %s을(를) 500원에 팔았다.%n", pen, pen));

		Thread.sleep(4000);
		contract.dispose();
	}

	@Test
	@DisplayName("지수의 의뢰")
	void completableFutureIsLazy() {
		String 이름 = "지슈";

		DynamicObject dynamicObject1 = new DynamicObject();
		DynamicObject dynamicObject2 = new DynamicObject();
		DynamicObject dynamicObject3 = new DynamicObject();

		인사하기(이름).thenCompose(인사 -> 악수하기(인사, dynamicObject1))
			.thenCompose(악수 -> 뽀뽀하기(악수, dynamicObject2))
			.thenCompose(뽀뽀 -> 잘가요인사하기(뽀뽀, dynamicObject3))
			.exceptionally(throwable -> {
				log.info(throwable.getMessage());
				return null;
			});
		System.out.println("dynamicObject3 is null ? " + (dynamicObject1.name));
		System.out.println("dynamicObject3 is null ? " + (dynamicObject2.name));
		System.out.println("dynamicObject3 is null ? " + (dynamicObject3.name));
	}

	@Test
	@DisplayName("& 연산자의 lazy evalution 성질")
	void andOperatorIsLazy() {
		boolean f = false;
		boolean t = true;
		System.out.println("\nCASE1");
		if (f && t == false)
			;
		System.out.println("\nCASE2");
		if (!(getFalse() && getTrue()))
			;

		System.out.println("\nCASE3");
		if (!(getFalse() && getTrue()))
			;

		System.out.println("\nCASE4");
		if (getTrue() || getFalse())
			;

	}

	boolean getFalse() {
		System.out.println("getFalse 실행됨");
		return false;
	}

	boolean getTrue() {
		System.out.println("getTrue 실행됨");
		return true;
	}

	public CompletableFuture<String> 인사하기(String name) {

		System.out.println("인사하기 실행");
		SleepUtil.sleepFor(1);
		return CompletableFuture.completedFuture(String.format("안녕 %s양", name));
	}

	public CompletableFuture<String> 악수하기(String greetingMessage, DynamicObject d) {
		System.out.println("악수하기 실행");
		d.setDynamicObject("악수하기 때 생성되는 객체");
		SleepUtil.sleepFor(1);
		return CompletableFuture.completedFuture(String.format(greetingMessage + " 이제 우리 악수할까?"));
	}

	public CompletableFuture<String> 뽀뽀하기(String handShakeMessage, DynamicObject d) {
		System.out.println("뽀뽀하기 실행");
		d.setDynamicObject("뽀뽀하기 때 생성되는 객체");
		SleepUtil.sleepFor(1);
		return CompletableFuture.failedFuture(new RuntimeException("언제 봤다고 뽀뽀에요! 뺨쌰대기"));
	}

	public CompletableFuture<String> 잘가요인사하기(String 뽀뽀한결과, DynamicObject d) {
		System.out.println("잘가요 인사하기 실행");
		d.setDynamicObject("잘가요인사하기 때 생성되는 객체");
		SleepUtil.sleepFor(1);
		return CompletableFuture.completedFuture(뽀뽀한결과 + " 이제 잘가ㅠㅠ");
	}

	public Mono<String> successfulJob() {
		return Mono.just("success");
	}

	public Mono<String> errorMono() {
		return Mono.error(new RuntimeException("실패한 작업"));
	}

	public Flux<String> errorFlux() {
		return Flux.error(new RuntimeException("실패한 작업"));
	}

	public Flux<Object> object_service() {
		return Flux.just("1", "2", "3", "4", "5");
	}

	public Mono<String> maybe_service() {
		return Mono.empty();
	}

	public Flux<Integer> numerical_service() {
		return Flux.range(1, 10);
	}

	public boolean passThrough(String s) {
		return true;
	}

	public boolean failedToPass(String s) {
		return false;
	}
}
