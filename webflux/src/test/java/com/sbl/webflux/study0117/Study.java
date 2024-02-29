package com.sbl.webflux.study0117;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sbl.webflux.Util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Study {

	@DisplayName("나쁜 예 파이프라인은 변수 말고 함수로 분리합시다.")
	@Test
	void BadCode1() {
		/**
		 * 변수명에 memberMono 변수를 포함하는 것 자체가 안티패턴
		 */
		Mono<Util.Member> memberMono = Util.ReactorUtil.getMember();
		Flux<Util.GameRecord> gameRecordFlux = Util.ReactorUtil.getGameRecords();

		gameRecordFlux.zipWith(memberMono)
			.map((tuple) -> String.format("%s 플레이어 %s의 최고 기록은 %d", tuple.getT1().getName(), tuple.getT2().getName(),
				tuple.getT1().getHighScore()))
			.subscribe(log::info);
	}

	@DisplayName("나쁜 예 파이프라인은 변수 말고 함수로 분리합시다.")
	@Test
	void BadCode2() {
		/**
		 * 그렇다고 변수명에 형식을 포함하지않으면 이게 무슨형식인지 까봐야 알 확률이 높다.
		 * 파이프라인 내에서 확인하기 쉽지 않음
		 * 복수,단수로 분리하는 것도 무리가 있음
		 */
		Mono<Util.Member> member = Util.ReactorUtil.getMember();
		Flux<Util.GameRecord> gameRecords = Util.ReactorUtil.getGameRecords();
		gameRecords.zipWith(member)
			.map((tuple) -> String.format("%s 플레이어 %s의 최고 기록은 %d", tuple.getT1().getName(), tuple.getT2().getName(),
				tuple.getT1().getHighScore()))
			.subscribe(log::info);
	}

	@DisplayName("좋은 예 파이프라인은 변수 말고 함수로 분리합시다.")
	@Test
	void GoodCode() {
		Util.ReactorUtil.getGameRecords()
			.zipWith(Util.ReactorUtil.getMember())
			.map((tuple) -> String.format("%s 플레이어 %s의 최고 기록은 %d", tuple.getT1().getName(), tuple.getT2().getName(),
				tuple.getT1().getHighScore()))
			.subscribe(log::info);
	}

	@DisplayName("캐시 사용시에는 예외적으로 사용")
	@Test
	void ExceptionallyAcceptable() {
		Mono<Util.Member> cachedMember = Util.ReactorUtil.getMember().cache();
		Flux<Util.GameRecord> cachedGameRecords = Util.ReactorUtil.getGameRecords().cache();
		cachedGameRecords.zipWith(cachedMember)
			.map((tuple) -> String.format("%s 플레이어 %s의 최고 기록은 %d", tuple.getT1().getName(), tuple.getT2().getName(),
				tuple.getT1().getHighScore()))
			.subscribe(log::info);
	}

	@DisplayName("캐시 사용 예제")
	@Test
	void noCache() throws InterruptedException {
		Flux<Util.Expensive> expensiveFlux = Util.ReactorUtil.getFluxAfter1Sec();
		Util.ReactorUtil.getGameRecords()
			.zipWith(expensiveFlux)
			.map((tuple) -> String.format("비싼 연산과 결합되고 구독된 GameRecord %s", tuple.getT1().getName()))
			.log()
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe(log::info);

		expensiveFlux.zipWith(Util.ReactorUtil.getMember())
			.map((tuple) -> String.format("비싼 연산과 결합되고 구독된 Member %s", tuple.getT2().getName()))
			.log()
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe(log::info);
	}

	@DisplayName("캐시 사용 예제")
	@Test
	void useCache() {

		Flux<Util.Expensive> cachedExpensive = Util.ReactorUtil.getFluxAfter1Sec().cache();

		Util.ReactorUtil.getGameRecords()
			.zipWith(cachedExpensive)
			.map((tuple) -> String.format("비싼 연산과 결합되고 구독된 GameRecord %s", tuple.getT1().getName()))
			.log()
			.subscribe(log::info);

		cachedExpensive.zipWith(Util.ReactorUtil.getMember())
			.map((tuple) -> String.format("비싼 연산과 결합되고 구독된 Member %s", tuple.getT2().getName()))
			.log()
			.subscribe(log::info);
	}

	/**
	 * filter되기 전의 방출된 값들을 알고싶다면 filter 위에 log를 찍자
	 */

	@DisplayName("log의 위치에 따른 변화")
	@Test
	void logPosition() {
		// filter 이후에 .log()
		Flux.just(1, 20, 300)
			.map(String::valueOf)
			.filter(s -> s.length() > 1)
			.log()
			.subscribe();
		// filter 이전에 .log()
		Flux.just(1, 20, 300)
			.map(String::valueOf)
			.log()
			.filter(s -> s.length() > 1)
			.subscribe();
	}

	@DisplayName("로그는 파이프라인 내에 위치시키자")
	@Test
	void badLog() throws InterruptedException {
		// Mono<String> badLoggingMono = getMonoWithDefaultLog();
		Mono<String> goodLoggingMono = getMonoWithLogInPipeline();
		Thread.sleep(3000);
		// badLoggingMono.subscribe(log::info);
		goodLoggingMono.subscribe(log::info);
	}

	public Mono<String> getMonoWithDefaultLog() {
		log.info("getMonoWithDefaultLog 실행");
		return Mono.just("값");
	}

	public Mono<String> getMonoWithLogInPipeline() {
		return Mono.just("값")
			.doOnSubscribe((subscription) -> log.info("getMonoWithLogInPipeline 실행"));
	}

	@Test
	void badFlatMap() {
		String str = "안녕";
		Mono<Void> work = firstTask(str)
			.flatMap(str2 -> secondTask(str))
			.then();

	}

	/**
	 * Mono.then,
	 * and 병렬적으로 처리해도되는경우 
	 * then 2개의 작업을 이어서 할때
	 * when 3개이상
	 */
	@Test
	void goodFlatMap() {
		String str = "안녕";
		Mono<Void> work = firstTask(str)
			.then(secondTaskNotReturns(str))
			.then();

	}

	private Mono<String> firstTask(String string) {
		return doSomeThing(string).thenReturn(string);
	}

	private Mono<String> secondTask(String string) {
		return doSomeThing(string).thenReturn(string);
	}

	private Mono<Void> secondTaskNotReturns(String string) {
		return doSomeThing(string);
	}

	private Mono<Void> doSomeThing(String string) {
		log.info("doSomeThing with " + string);
		return Mono.empty();
	}

	@Test
	void customPubSub() {
		Util.ReactorUtil.CustomPublisher publisher = new Util.ReactorUtil.CustomPublisher(
			IntStream.rangeClosed(1, 5));
		Util.ReactorUtil.CustomSubscriber subscriber = new Util.ReactorUtil.CustomSubscriber(Long.MAX_VALUE);
		publisher.subscribe(subscriber);
	}

	// 숫자들 중 5보다 크면서 2~3번째 요소 출력
	@Test
	void streamExcersize() {
		IntStream.rangeClosed(1, 10).filter(i -> i > 5).skip(1).limit(2).forEach(i -> log.info(String.valueOf(i)));
	}

	@Test
	void streamMapAndFlatMap() {

		IntStream.rangeClosed(1, 7)
			.boxed()
			.map(id -> Util.getMemberStream().filter(member -> member.getId() == id))
			.forEach(member -> log.info(member.toString()));

		IntStream.rangeClosed(1, 7)
			.boxed()
			.flatMap(id -> Util.getMemberStream().filter(member -> member.getId() == id))
			.forEach(member -> log.info(member.toString()));
	}

	/**
	 * main Thread가 subscribe에 구독한 사실과 consumer function을 전달하고나서 종료됨
	 * daemon thread로 실행되는 Flux element의 방출과 구독한 consumerfunction의 실행은 진행되지 않는다.
	 */
	@Test
	void fluxPrintWithDaemonThread() {
		Flux<Integer> intFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));
		intFlux.subscribe(i -> log.info(i.toString()));
	}

	/**
	 * main Thread가 직접 sleep하며 방출하기때문에 blocking, 동기 방식으로 작동함
	 */
	@Test
	void fluxPrintWithMainThread() {
		Flux<Long> flux = Flux.create(emitter -> {
			for (long i = 1; i <= 5; i++) {
				// 요소를 방출
				emitter.next(i);
				// 1초 대기
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// 작업 완료를 알림
			emitter.complete();
		});

		flux.subscribe(i -> log.info(String.valueOf(i)));
	}

	@Test
	void notPubSub() {
		log.info("누가 Flux의 요소를 방출할거지?");
		Flux.range(1, 10000).subscribe(i -> log.info(String.valueOf(i)));
	}

	/**
	 * main Thread에서 요소를 방출하고, 방출된 요소가 출력되는 consumer function은 boundedElastic 데몬 쓰레드가 수행할 것
	 * 따라서 main Thread가 아주 조금 방출하다가 종료되면 consumer function을 수행하는 쓰레드도 종료된다.
	 */
	@Test
	void pub() {
		log.info("누가 Flux의 요소를 방출할거지?");
		Flux.range(1, 10000).publishOn(Schedulers.boundedElastic()).log().subscribe(i -> log.info(String.valueOf(i)));
	}

	@Test
	void sub() {
		log.info("누가 Flux의 요소를 방출할거지?");
		Flux.range(1, 10000).subscribeOn(Schedulers.boundedElastic()).log().subscribe(i -> log.info(String.valueOf(i)));
	}

	@Test
	void fluxPrintWithSleep() throws InterruptedException {
		log.info("함수 실행");
		Flux<Integer> intFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));
		intFlux.subscribe(i -> log.info(i.toString()));
		Thread.sleep(6000);
	}

	// block하면서, Flux에서 요소가 방출될때마다 forEach로 전달한 Consumer function이 작동함
	@Test
	void fluxToStream() {
		Flux<Integer> intFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));
		intFlux.toStream().forEach(i -> log.info(String.valueOf(i)));
	}

	// 리스트는 변환하는경우 완전히 block 되고, Flux 요소의 방출이 종료되는시점에 리스트가 완성된다.
	@Test
	void fluxToList() {
		Flux<Integer> intFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));
		intFlux.toStream().toList().forEach(i -> log.info(String.valueOf(i)));
	}

	@Test
	void allHooks() {
		Flux<Integer> flux = Flux.range(1, 100);

		Consumer<Integer> itemConsumer = new Consumer<Integer>() {
			@Override
			public void accept(Integer integer) {
				log.info(integer.toString());
			}
		};
		Consumer<Throwable> errorConsumer = new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) {
				log.error("error 발생 {}", throwable.getMessage());
			}
		};

		Runnable completionEventRunnable = new Runnable() {
			@Override
			public void run() {
				log.info("끝났음");
			}
		};
		flux.subscribe(itemConsumer, errorConsumer, completionEventRunnable);
	}

	@Test
	void block() {
		Object foo = Util.ReactorUtil.notEmitMono().block();
	}

	@Test
	void blockButGiveUp() {
		Object foo = Util.ReactorUtil.notEmitMono().block(Duration.ofSeconds(5));
	}

}
