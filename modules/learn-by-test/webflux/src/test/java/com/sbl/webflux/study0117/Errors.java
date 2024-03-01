package com.sbl.webflux.study0117;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.sbl.webflux.Util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Slf4j
public class Errors {

	@Test
	void timeOut() {
		Util.ReactorUtil.getFluxEmitBy1Sec()
			.log()
			.timeout(Duration.ofMillis(500))
			.subscribe();
	}

	/**
	 * 에러 발생시 수행할 side effect에 대해서 지정하고 시퀀스가 종료되기를 원한다면
	 * doOnError
	 */
	@Test
	void doOnError() {
		Flux.range(1, 5)
			.log()
			.map(i -> {
				try {
					return convert(i);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.doOnError(error -> log.error("doOnError에서 캐치" + error.getMessage()))
			.subscribe(str -> log.info("시퀀스 완료 :{}", str));
	}

	/**
	 * 에러 발생시 logging 등을 수행하고 시퀀스를 그대로 진행하고싶은경우
	 * onErrorContinue
	 */
	@Test
	void onErrorContinue() {
		Flux.range(1, 5)
			.log()
			.map(i -> {
				try {
					return convert(i);
				} catch (IOException e) {
					throw Exceptions.propagate(e);
				}
			})
			.onErrorContinue((error, value) -> {
				// 에러가 발생하면 로그 출력
				System.err.println("Error occurred: " + error.getMessage());
			})
			.subscribe(str -> log.info("시퀀스 완료 :{}", str));
	}

	/**
	 * 에러 발생시 에러가 발생한 요소 대신 대체대이터를제공한후 시퀀스를 종료한다.
	 */
	@Test
	void onErrorResume() {
		Flux.range(1, 5)
			.log()
			.map(i -> {
				try {
					return convert(i);
				} catch (IOException e) {
					throw new Util.MyError();
				}
			})
			.onErrorResume(throwable -> {
				// 에러가 발생하면 대체 데이터를 제공
				System.err.println("Error occurred: " + throwable.getMessage());
				return Flux.just("asdf"); // 대체 데이터
			})
			.subscribe(str -> log.info("시퀀스 완료 :{}", str));
	}

	/**
	 * retry는 업스트림을 재구독한다. elapsed() 메서드를 통해 알아보자
	 * map에서 예외를 throw할때까지 걸리는 시간 250ms,
	 * retry를 만나서 재시도하여 업스트림을 재구독하고 첫번째 요소가 방출될때까지 걸리는 시간 250ms,
	 * 즉 retry로 인해 onNext -> cancel -> onSubscribe -> request -> onNext(새로운 요소의 시작) 의 총 과정이 500ms가 걸림
	 */
	@Test
	void reSubscribeWhenRetry() throws InterruptedException {
		Flux.interval(Duration.ofMillis(250))
			.log()
			.map(input -> {
				if (input < 3)
					return "tick " + input;
				throw new RuntimeException("retry 1회 이후의 구독은 예외를 throw한다.");
			})
			.retry(1)
			.elapsed() // (1)
			.subscribe(System.out::println, System.err::println); // (2)

		Thread.sleep(3000); // (3)
	}

	@Test
	void retryWhen() {
		Flux<String> flux = Flux
			.<String>error(new RuntimeException())
			.log()
			.doOnError(e -> log.error(String.valueOf(e)))
			.retryWhen(Retry.max(3));

		flux.subscribe();

		Flux<String> flux2 = Flux
			.<String>error(new RuntimeException())
			.log()
			.doOnError(e -> log.error(String.valueOf(e)))
			.retry(3);

		flux2.subscribe();
	}

	private String convert(Integer integer) throws IOException {
		if (integer > 3) {
			throw new IOException(String.format("%d 변환 중 IOException 발생", integer));
		}
		return integer + "문자열로 변환됨";
	}
}
