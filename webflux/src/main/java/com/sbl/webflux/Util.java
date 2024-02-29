package com.sbl.webflux;

import java.time.Duration;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Util {
	public static Stream<Member> getMemberStream() {
		return Stream.of(Member.builder().id(1).name("서병렬").build(), Member.builder().id(2).name("이상문").build(),
			Member.builder().id(3).name("홍이").build(), Member.builder().id(4).name("박정민").build(),
			Member.builder().id(5).name("남궁철").build());
	}

	@Data
	@Builder
	public static class Expensive {
		private long id;
		private String name;
	}

	@Data
	@Builder
	public static class GameRecord {
		private long id;
		private String name;
		private int highScore;
	}

	@Data
	@Builder
	public static class Member {
		private long id;
		private String name;
	}

	public static class MyError extends RuntimeException {
		public MyError() {
			System.out.println("MyError 객체가 생성되었습니다.");
		}
	}

	@Slf4j
	public static class ReactorUtil {

		public static Mono<Member> getMember() {
			return Mono.create(memberMonoSink -> {
				Member member = Member.builder().id(1).name("서병렬").build();
				memberMonoSink.success(member);
			});
		}

		public static Flux<GameRecord> getGameRecords() {
			GameRecord gameRecord1 = GameRecord.builder().id(1).name("리그오브레전드").highScore(700).build();
			GameRecord gameRecord2 = GameRecord.builder().id(2).name("바둑").highScore(2).build();
			return Flux.just(gameRecord1, gameRecord2);
		}

		public static Flux<Expensive> getFluxAfter1Sec() {
			log.info("열일중....");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return Flux.just(Expensive.builder().id(1).name("비싼작업 내용물1").build(),
				Expensive.builder().id(2).name("비싼작업 내용물2").build());
		}

		public static Flux<String> getFluxEmitBy1Sec() {
			return Flux.just("abc", "def", "ghi").delayElements(Duration.ofSeconds(1));
		}

		public static Mono<String> notEmitMono() {
			return Mono.create(emitter -> {
				if (false) {
					emitter.success("성공");
				}
			});
		}

		public static Mono<String> encryptPasswordMono(String password) {
			log.info("encryptPassword 실행");
			return Mono.just(password + "까꿍");
		}

		public static String encryptPassword(String password) {
			log.info("encryptPassword 실행");
			return password + "까꿍";
		}

		public static Mono<String> login(String t1, String t2) {
			//어?쩌구 쿵쾅쿵쾅 로직
			return Mono.just("로그인에 성공했으니 jwt토큰을 받자");
		}

		public static Mono<String> getKey() {
			return Mono.just("sbl1998");
		}

		public static Mono<String> getPassword() {
			log.info("get Password 실행");
			return Mono.just("qwer1234");
		}

		public static Flux<String> reactiveOperation(Integer id) {
			return Flux.just(String.valueOf(id * 2));
		}

		public static Mono<String> toMono(Integer id) {
			return Mono.just(String.valueOf(id * 2));
		}

		public static Mono<String> getName(int id) {
			if (id == 1)
				return Mono.just("john");
			return Mono.just("Alice");
		}

		public static Mono<Integer> integerMono() {
			return Mono.just(7);
		}

		public static Mono<String> stringMono(Integer integer) {
			return Mono.just(integer.toString());
		}

		public static Mono<String> stringRepeatMono(String str) {
			return Mono.just(str.repeat(3));
		}

		public static class CustomPublisher implements Publisher<Integer> {
			private final IntStream data;

			public CustomPublisher(IntStream data) {
				this.data = data;
			}

			@Override
			public void subscribe(Subscriber<? super Integer> subscriber) {
				CustomSubscription subscription = new CustomSubscription(subscriber, data.iterator());
				subscriber.onSubscribe(subscription);
			}
		}

		public static class CustomSubscription implements Subscription {
			private final Subscriber<? super Integer> subscriber;
			private final PrimitiveIterator.OfInt dataIterator;
			private boolean completed;

			CustomSubscription(Subscriber<? super Integer> subscriber, PrimitiveIterator.OfInt dataIterator) {
				this.subscriber = subscriber;
				this.dataIterator = dataIterator;
				this.completed = false;
			}

			@Override
			public void request(long n) {
				log.info("request({})", n);
				try {
					while (n-- > 0 && dataIterator.hasNext()) {
						subscriber.onNext(dataIterator.nextInt());
					}

					if (!dataIterator.hasNext() && !completed) {
						completed = true;
						subscriber.onComplete();
					}
				} catch (Exception e) {
					subscriber.onError(e);
				}
			}

			@Override
			public void cancel() {
				// 취소 로직
			}
		}

		public static class CustomSubscriber implements Subscriber<Integer> {
			private Subscription subscription;
			private long initialRequest;

			public CustomSubscriber(long initialRequest) {
				this.initialRequest = initialRequest;
			}

			@Override
			public void onSubscribe(Subscription subscription) {
				log.info("onSubscribe({})", subscription.getClass());
				this.subscription = subscription;
				subscription.request(initialRequest); // 첫 아이템 요청
			}

			@Override
			public void onNext(Integer item) {
				log.info("onNext({}) ", item);
				// subscription.request(1); // 다음 아이템 요청
			}

			@Override
			public void onError(Throwable throwable) {
				System.err.println("Error occurred: " + throwable.getMessage());
			}

			@Override
			public void onComplete() {
				log.info("onComplete");
			}
		}
	}

	public static class DynamicError extends RuntimeException {
		public DynamicError(String dynamicString) {
			super(dynamicString);
			System.out.println("DynamicError 생성됨 " + dynamicString);
		}
	}

	public static class Pen {
		String color;

		public Pen(String color) {
			this.color = color;
		}

		public boolean checkQuality() {
			return !Objects.equals("불량", this.color);
		}

		@Override
		public String toString() {
			return this.color + "색 펜";
		}
	}
}
