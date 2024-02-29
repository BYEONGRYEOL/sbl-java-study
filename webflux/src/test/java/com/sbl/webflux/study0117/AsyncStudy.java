package com.sbl.webflux.study0117;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncStudy {
	@Test
	void returnBooleanAsyncNotWorking() {
		int[] input = IntStream.rangeClosed(1, 1000).toArray();

		for (int i = 0; i < input.length; i++) {
			boolean isEven = isEvenNumber(input[i]);

			if (isEven)
				log.info("YES");
			else
				log.info("NO");
		}
	}

	/**
	 * @Async가 붙은 메서드의 리턴형식은 void or Future-Like 여야 한다
	 * 이런식이면 그냥 Main Thread에서 작업함
	 */
	@Async
	public boolean isEvenNumber(int n) {
		log.info("a");
		return (n % 2 == 0);
	}

	@Test
	void returnVoidAsyncNotWorking() {
		int[] input = IntStream.rangeClosed(1, 1000).toArray();

		for (int i = 0; i < input.length; i++) {
			printIsEvenNumber(input[i]);
		}
	}

	@Async
	public void printIsEvenNumber(int n) {
		log.info(n % 2 == 0 ? "YES" : "NO");
	}

	@Test
	void returnFutureLikeAsync() throws ExecutionException, InterruptedException {
		for (int i = 0; i < 1000; i++) {
			log.info(d().get());
		}
	}

	@Async
	public CompletableFuture<String> d() {
		return CompletableFuture.completedFuture("비동기?");
	}

}
