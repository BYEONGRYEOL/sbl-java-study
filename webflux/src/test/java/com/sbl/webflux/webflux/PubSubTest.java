package com.sbl.webflux.webflux;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class PubSubTest {

	static SchedulerA schedulerA = new SchedulerA();
	static SchedulerB schedulerB = new SchedulerB();

	/**
	 * @Author 서병렬
	 * @Description ReactiveStreams의 Flux나 Mono 등 Publisher를 이용하고
	 * Subscriber를 등록하여 데이터 흐름이 발생하게했을 때,
	 * Publish -> subscribe 까지의 작업에 대해서 publishOn, subscribeOn으로
	 * 작업을 수행할 스케쥴러를 지정해주지 않으면 publisher이전에 작업을 수행하고있던 쓰레드가
	 * publish -> subscribe 체인 모두를 수행한다.
	 * 즉 비동기로 수행되는 것이 아님
	 * @ResultAnalysis pubsub chain의 모든 작업을 Test worker 쓰레드가 수행한다.
	 */
	@Test
	void pubsubWithoutScheduler() {
		log.info("publish, subscribe시에 특별히 지정하지 않으면 모든 작업을 싱글스레드 -> 즉 동기로 작업한다.");
		Flux.range(1, 3).map(i -> {
			log.info("first map");
			return i;
		}).map(i -> {
			log.info("second map");
			return i;
		}).subscribe(i -> {
			log.info("subscribe");
		});
	}

	/**
	 * @Author 서병렬
	 * @Description publishOn으로 스케쥴러를 지정할 경우 publishOn 이후 "생성" 작업을 지정된 스케쥴러가 역임한다.
	 * publishOn 이후의 생성 작업을 boudedElastic 스케쥴러가 맡은 것이므로, Map.2, Map.1 에서 요청할때부터 Map.1에의해 1,2,3 의 데이터로 10,20,30이 생성될 때 까지는 boundedElastic 스케쥴러와 상관없이
	 * 기존에 테스트를 수행하던 Test Worker 쓰레드가 수행한다.
	 * <p>
	 * 뒤에 subscribeOn 메서드에 대한 테스트를 보면 더 확실하게 이해할 수 있음
	 * ++ 빠른 publisher와 느린 subscriber로 chain이 구성될 때 사용함
	 * @ResultAnalysis Schedulers.boundedElastic() 이후 2번째 map 메서드의 작업은 boundedElascit 스케쥴러의 쓰레드가 역임한다.
	 */
	@Test
	void publishOnOnceBoundedElastic() {
		log.info("빠른 publisher와 느린 subscriber로 chain이 구성될 때 사용함");
		Flux.range(1, 3)
			.map(i -> i * 10)
			.log()
			.publishOn(Schedulers.boundedElastic())
			.map(i -> i * 10)
			.log()
			.subscribe();
	}

	/**
	 * @Author 서병렬
	 * @Description publishOn으로 스케쥴러를 지정할 경우 기본적으로 publishOn 이후 생성 작업을 지정된 스케쥴러가 역임한다.
	 * 그러면 publishOn이 두번 이상 사용된 경우에는? 상상하는 대로 스케쥴링이 되긴 하지만 다른 점이 하나 있다.
	 * 서술식 순서와 실제 스케쥴링 되는 순서는 같지 않다.
	 * 무슨 말이냐면, pubsub chain을 따라서 사람의 눈으로 읽히는 순서로 스케쥴링하는 것이 아니라
	 * 최종 subscribe될때부터 거슬러 올라가가면서 어떤 스케쥴러에게 작업을 역임할것인지 결정한다.
	 * @ResultAnalysis 스케쥴러B에서 스케쥴러A에서보다 빨리 스케쥴링 되고, 실제 작업순서는 스케쥴러A의 쓰레드가 먼저임
	 * 스케쥴링되는 순서는 다음과 같다. (아래에서부터 진행된다.)
	 * publishOn(schedulerB) 에서 스케쥴러B에게 map-3, subscribe 작업이 할당,
	 * publishOn(schedulerA) 에서 스케쥴러A에게  map-2, map-3, subscribe 작업들 중 스케쥴러B에게 할당되지 않은 map-2 작업이 할당,
	 * 그 위의 map-1 작업은 스케쥴러가 따로 지정되지 않아서 Test Worker가 수행한다.
	 */
	@Test
	void publishOnTwiceWithCustomScheduler() {
		SchedulerA schedulerA = new SchedulerA();
		SchedulerB schedulerB = new SchedulerB();
		Flux.range(1, 5).map(i -> {
			log.info("i : " + i);
			return i * 10;
		}).publishOn(schedulerA).map(i -> {
			log.info("i : " + i);
			return i * 10;
		}).publishOn(schedulerB).map(i -> {
			log.info("i : " + i);
			return i * 10;
		}).subscribe(i -> {
			log.info("i : " + i);
		});
	}

	/**
	 * @Author 서병렬
	 * @Description publishOnTwiceWithCustomScheduler() 테스트와 동일하지만 직접 정의한 스케쥴러가 아니라
	 * Schedulers.boudedElastic()로 CachedScheduler를 사용한다.
	 * @ResultAnalysis 스케쥴링 순서와 서술식 순서가 반대이므로,
	 * MapFuseable.2 작업중인 쓰레드가 boundedElastic-2 이고,
	 * MapFuseable.3 작업중인 쓰레드가 boundedElastic-1 임을 확인할 수 있다.
	 */
	@Test
	void publishOnTwiceWithBoundedElasticScheduler() {
		Flux.range(1, 5)
			.map(i -> i * 10)
			.log()
			.publishOn(Schedulers.boundedElastic())
			.map(i -> i * 10)
			.log()
			.publishOn(Schedulers.boundedElastic())
			.map(i -> i * 10)
			.log()
			.subscribe(i -> {
			});
	}

	/**
	 * @Author 서병렬
	 * @Description subscribeOn 메서드로 스케쥴러를 지정하면 해당 pubsub chain의 단계에서
	 * 상위 chain에 데이터 생성 요청 작업이후부터 모든 작업을 해당 스케쥴러에게 역임한다.
	 * 이전에도 설명했지만, 스케쥴러에게 작업을 스케쥴링하는 작업은 pubsub chain의 가장 밑단인 subscribe부터시작된다.
	 * 조금 더 정확하게 말하면, subscriber가 publisher에게 "request" 하여 데이터의 생성을 요청하고,
	 * 실제로 데이터를 생성하는 publisher를 만나기 전까지 단계적으로 계속해서 요청을 보내게된다.
	 * 아래 테스트 코드에서 .subscribe(sum::addAndGet); 코드를 처음으로 마주할 쓰레드가 누구인지 생각해보자
	 * ++ 느린 publisher와 빠른 subscriber의 경우에 사용되면 좋다.
	 * @ResultAnalysis map-2 와 map-3 사이에 subscribeOn(schedulerA) 스케쥴러 A에게 subscribeOn 하는 코드가 있다.
	 * Map.3 subscribe -> Map.3 request -> 이후에 map-2와 map-3 사이에 존재하는 subscribeOn(schedulerA) 코드가 실행되면서
	 * Map.2에서 Map.1으로 request할때부터는 스케쥴러A가 작업을 담당한다.
	 * map-2 와 map-3 사이에서 subscribeOn(schedulerA) 이 10부터 50을 생성할 때 까지,
	 * 그리고, Map.1,2,3 에서 onComplete()가 실행될 때 까지 모두 스케쥴러A에 의해 생성된 쓰레드가 작업할 것이다.
	 * <p>
	 * 추가적으로 AtomicInteger를 이용하여 비동기로 수행되는 작업임을 알 수 있도록 약간의 테스트로직을 추가했다.
	 * TestWorker가 Map.3에서 request를 보낸 이후로 스케쥴러A에게 이후의 pubsub chain의 작업을 역임하고
	 * TestWorker는 바로 pubsub  체인 이후의 작업을 수행한다. 따라서 아직 pubsub chain의 작업이
	 * request중이거나, 이제 Map.1에서 생산중일 시점에 sum은 초깃값 0인상태일 것이고,
	 * TestWorker가 1초간 sleep한 이후에는 pubsub chain의 모든 작업이 끝났을 것이므로 마지막으로 subscribe 하고있는
	 * sum::addAndGet 메서드가 실행되어 sum의 값에 생산한 데이터들이 더해졌을 것.
	 */
	@Test
	void subscribeOn() throws InterruptedException {
		AtomicInteger sum = new AtomicInteger(0);
		Flux.range(1, 5)
			.map(i -> i * 10)
			.log()
			.map(i -> i * 10)
			.log()
			.subscribeOn(schedulerA)
			.map(i -> i * 10)
			.log()
			.subscribe(sum::addAndGet);
		log.info("sum.get() : " + sum.get());
		assertThat(sum.get()).isEqualTo(0);
		Thread.sleep(1000L);
		log.info("sum.get() : " + sum.get());
		assertThat(sum.get()).isEqualTo(1000 + 2000 + 3000 + 4000 + 5000);
	}

	/**
	 * @Author 서병렬
	 * @Description subscribeOn 메서드로 스케쥴러를 지정하면 해당 pubsub chain의 단계에서 상위 chain에 데이터 생성 요청을 하는 것 이후부터
	 * 모든 작업을 해당 스케쥴러에게 역임한다.
	 * subscribeOn이 두개라면? 무조건 최근에 스케쥴링된 스케쥴러가 우선권을 갖는다.
	 * 정확하게 말하면 먼저 스케쥴링되어 A스케쥴러의 A-1쓰레드가 .subscribeOn(schedulerB) 라인을 만났다면,
	 * A스케쥴러의 A-1쓰레드가 B스케쥴러의 B-1 쓰레드에게 이후의 작업을 위임하게된다.
	 * @ResultAnalysis chain을 거슬러 올라가며 	.subscribeOn(schedulerB) 라인을 만나기 전까진
	 * 작업의 위임 로직이 없으므로 TestWorker 쓰레드가 상위 publisher에게 request하고,
	 * .subscribeOn(schedulerB) 이후에는 스케쥴러 B가 상위 publisher에게 request,
	 * .subscribeOn(schedulerA) 라인에서 이후의 생성 요청 작업부터 이후의 모든 작업들은 스케쥴러 A가 역임한다.
	 * 이제 서술식으로 스케쥴링되지 않는다는 것을 이해했겠지만, 여전히 스케쥴러 B가 Map.2에서 작업하지않을까하는 생각이 들면 절대 안된다.
	 */
	@Test
	void subscribeOnTwice() throws InterruptedException {
		Flux.range(1, 2)
			.log()
			.subscribeOn(schedulerA)
			.map(i -> i * 10)
			.log()
			.subscribeOn(schedulerB)
			.map(i -> i * 10)
			.log()
			.subscribe();
	}

	/**
	 * @Author 서병렬
	 * @Description subscribeOn 메서드로 스케쥴러를 지정하면 해당 pubsub chain의 단계에서 상위 chain에 데이터 생성 요청을 하는 것 이후부터
	 * 모든 작업을 해당 스케쥴러에게 역임한다.
	 * subscribeOn이 두개라면? 무조건 최근에 스케쥴링된 스케쥴러가 우선권을 갖는다.
	 * 정확하게 말하면 먼저 스케쥴링되어 A스케쥴러의 A-1쓰레드가 .subscribeOn(schedulerB) 라인을 만났다면,
	 * A스케쥴러의 A-1쓰레드가 B스케쥴러의 B-1 쓰레드에게 이후의 작업을 위임하게된다.
	 * @ResultAnalysis chain을 거슬러 올라가며 	.subscribeOn(schedulerB) 라인을 만나기 전까진
	 * 작업의 위임 로직이 없으므로 TestWorker 쓰레드가 상위 publisher에게 request하고,
	 * .subscribeOn(schedulerB) 이후에는 스케쥴러 B가 상위 publisher에게 request,
	 * .subscribeOn(schedulerA) 라인에서 이후의 생성 요청 작업부터 이후의 모든 작업들은 스케쥴러 A가 역임한다.
	 * 이제 서술식으로 스케쥴링되지 않는다는 것을 이해했겠지만, 여전히 스케쥴러 B가 Map.2에서 작업하지않을까하는 생각이 들면 절대 안된다.
	 */
	@Test
	void subscribeAfterPublish() throws InterruptedException {
		// 혼합해서 사용하면 publishOn에 의해 분리된 스케쥴러가 먼저 쓰레드를 생성한다.
		Flux.range(1, 5)
			.log()
			.map(i -> i * 10)
			.publishOn(schedulerA)
			.log()
			.map(i -> i * 10)
			.subscribeOn(schedulerB)
			.log()
			.map(i -> i * 10)
			.subscribe();

		Thread.sleep(1000L);
	}

	@Test
	void publishAfterSubscribe() {

		//chain의 뒤에서 부터 이해하기,
		// schedulerB가 map3, subscribe 작업을 맡기로 했다.
		// schedulerA가 앞뒤 체인들중 schedule 되지 않은 task들 모두룰 가져간다.
		/**
		 * map1 : a
		 * map2 : a
		 * map3 : b
		 * subscribe : b
		 */

		// 혼합해서 사용하면 publishOn에 의해 분리된 스케쥴러가 먼저 쓰레드를 생성한다.

		Flux.range(1, 5).map(i -> {
			System.out.println("쓰레드 : " + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).subscribeOn(schedulerA).map(i -> {
			System.out.println("쓰레드 : " + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).publishOn(schedulerB).map(i -> {
			System.out.println("쓰레드 : " + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).subscribe(i -> {
			System.out.println("쓰레드 : " + Thread.currentThread().getName() + " " + i);
		});
	}

	@Test
	void publishOuterPublishInnerWithBondedElasticScheduler() {
		// inner Flux에서 publish한 구역이 nestes map 2 이후이므로 nested map 1 에는 스케쥴러가 할당되지 않았음
		// outer Flux에서 map 2 이후로 publish 하기로했으므로, outer flux map 2 & inner flux nested map 1 만큼을 역임한다.
		// 스케쥴러를 부여받지 못한 map 1 task는 main thread가 수행한다.
		Flux.range(1, 3).map(i -> {
			System.out.println("1st map():" + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).publishOn(Schedulers.boundedElastic()).map(i -> {
			System.out.println("2nd map():" + Thread.currentThread().getName() + " " + i);
			return Flux.fromIterable(Arrays.asList(1, 2)).map(j -> {
				System.out.println("nested 1st map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).publishOn(Schedulers.boundedElastic()).map(j -> {
				System.out.println("nested 2nd map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribe(j -> {
				System.out.println("subscribe():" + Thread.currentThread().getName() + " " + i + " " + j);
			});
		}).subscribe();

		/**
		 1st map():Test worker 1
		 1st map():Test worker 2
		 1st map():Test worker 3

		 publishOn에 의해 스케쥴링, 첫번째 쓰레드가 작업을 시작한다.
		 2nd map():boundedElastic-1 10
		 2nd map():boundedElastic-1 20
		 2nd map():boundedElastic-1 30
		 nested 1st map():boundedElastic-1 10 1
		 nested 1st map():boundedElastic-1 10 2

		 bondedElastic 스케쥴러에게 다시 publish, 두번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-2 10 10
		 nested 2nd map():boundedElastic-2 10 20
		 subscribe():boundedElastic-2 10 100
		 subscribe():boundedElastic-2 10 200

		 nested 1st map():boundedElastic-1 20 1
		 nested 1st map():boundedElastic-1 20 2

		 boundedElastic 스케쥴러에게 다시 publish, 세번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-3 20 10
		 nested 2nd map():boundedElastic-3 20 20
		 subscribe():boundedElastic-3 20 100
		 subscribe():boundedElastic-3 20 200


		 nested 1st map():boundedElastic-1 30 1
		 nested 1st map():boundedElastic-1 30 2

		 boundedElastic 스케쥴러에게 다시 publish, 네번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-4 30 10
		 nested 2nd map():boundedElastic-4 30 20
		 subscribe():boundedElastic-4 30 100
		 subscribe():boundedElastic-4 30 200
		 */
	}

	@Test
	void publishOuterPublishInnerWithCustomScheduler() {

		// nested 1st map 이후에 publishOn(schedulerB) 코드에 의해서 각 작업이 다른 쓰레드에게 역임되고있음

		// 내부적으로는 schedulerB 가 nested map2, nested subscribe 작업을 맡기로 하고 거슬러 올라가서
		// map2, nested map1 을 schedulerA가 맡게된다. 따라서 schedulerA 가 map2 부분에서  Flux.map 작업을 return되는 Flux.map 3개에 대해서 수행해야하므로
		// 각 3개의 task를 아까 역임하기로한 schedulerB에게 전달한다. 따라서 schedulerB는 3개의 쓰레드를 운용하게됨
		Flux.range(1, 3).map(i -> {
			System.out.println("1st map():" + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).publishOn(schedulerA).map(i -> {
			System.out.println("2nd map():" + Thread.currentThread().getName() + " " + i);
			return Flux.fromIterable(Arrays.asList(1, 2)).map(j -> {
				System.out.println("nested 1st map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).publishOn(schedulerB).map(j -> {
				System.out.println("nested 2nd map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribe(j -> {
				System.out.println("subscribe():" + Thread.currentThread().getName() + " " + i + " " + j);
			});
		}).subscribe();

		/**
		 1st map():Test worker 1
		 1st map():Test worker 2
		 1st map():Test worker 3

		 publishOn에 의해 스케쥴링, 첫번째 쓰레드가 작업을 시작한다.
		 2nd map():boundedElastic-1 10
		 2nd map():boundedElastic-1 20
		 2nd map():boundedElastic-1 30
		 nested 1st map():boundedElastic-1 10 1
		 nested 1st map():boundedElastic-1 10 2

		 bondedElastic 스케쥴러에게 다시 publish, 두번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-2 10 10
		 nested 2nd map():boundedElastic-2 10 20
		 subscribe():boundedElastic-2 10 100
		 subscribe():boundedElastic-2 10 200

		 nested 1st map():boundedElastic-1 20 1
		 nested 1st map():boundedElastic-1 20 2

		 boundedElastic 스케쥴러에게 다시 publish, 세번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-3 20 10
		 nested 2nd map():boundedElastic-3 20 20
		 subscribe():boundedElastic-3 20 100
		 subscribe():boundedElastic-3 20 200


		 nested 1st map():boundedElastic-1 30 1
		 nested 1st map():boundedElastic-1 30 2

		 boundedElastic 스케쥴러에게 다시 publish, 네번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-4 30 10
		 nested 2nd map():boundedElastic-4 30 20
		 subscribe():boundedElastic-4 30 100
		 subscribe():boundedElastic-4 30 200
		 */
	}

	@Test
	void subscribeOuterPublishInnerWithCustomScheduler() throws InterruptedException {
		// 특이한 점은 test worker Thread를 sleep해서 대기하지않으면 모두 출력되지 않는다는것.

		// inner Flux에 대해서 nested map 1 이후의 작업은 publishOn(schedulerB) 에 의해 schedulerB가 맡게 된다.
		// outer Flux에 대해서는 map1과 map2 사이에 publishOn(schedulerA)에 의해 map2작업부터 schedulerA가 맡게 된다.
		Flux.range(1, 3).map(i -> {
			System.out.println("1st map():" + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).subscribeOn(schedulerA).map(i -> {
			System.out.println("2nd map():" + Thread.currentThread().getName() + " " + i);
			return Flux.fromIterable(Arrays.asList(1, 2)).map(j -> {
				System.out.println("nested 1st map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).publishOn(schedulerB).map(j -> {
				System.out.println("nested 2nd map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribe(j -> {
				System.out.println("subscribe():" + Thread.currentThread().getName() + " " + i + " " + j);
			});
		}).subscribe();

		Thread.sleep(1000L);
		/**
		 1st map():Test worker 1
		 1st map():Test worker 2
		 1st map():Test worker 3

		 publishOn에 의해 스케쥴링, 첫번째 쓰레드가 작업을 시작한다.
		 2nd map():boundedElastic-1 10
		 2nd map():boundedElastic-1 20
		 2nd map():boundedElastic-1 30
		 nested 1st map():boundedElastic-1 10 1
		 nested 1st map():boundedElastic-1 10 2

		 bondedElastic 스케쥴러에게 다시 publish, 두번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-2 10 10
		 nested 2nd map():boundedElastic-2 10 20
		 subscribe():boundedElastic-2 10 100
		 subscribe():boundedElastic-2 10 200

		 nested 1st map():boundedElastic-1 20 1
		 nested 1st map():boundedElastic-1 20 2

		 boundedElastic 스케쥴러에게 다시 publish, 세번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-3 20 10
		 nested 2nd map():boundedElastic-3 20 20
		 subscribe():boundedElastic-3 20 100
		 subscribe():boundedElastic-3 20 200


		 nested 1st map():boundedElastic-1 30 1
		 nested 1st map():boundedElastic-1 30 2

		 boundedElastic 스케쥴러에게 다시 publish, 네번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-4 30 10
		 nested 2nd map():boundedElastic-4 30 20
		 subscribe():boundedElastic-4 30 100
		 subscribe():boundedElastic-4 30 200
		 */
	}

	@Test
	void subscribeOuterSubscribeInnerWithCustomScheduler() {

		// inner Flux에 대해서 nested map 1 이후의 작업은 publishOn(schedulerB) 에 의해 schedulerB가 맡게 된다.
		// outer Flux에 대해서는 map1과 map2 사이에 publishOn(schedulerA)에 의해 map2작업부터 schedulerA가 맡게 된다.
		Flux.range(1, 3).map(i -> {
			System.out.println("1st map():" + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).subscribeOn(schedulerA).map(i -> {
			System.out.println("2nd map():" + Thread.currentThread().getName() + " " + i);
			return Flux.fromIterable(Arrays.asList(1, 2)).map(j -> {
				System.out.println("nested 1st map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribeOn(schedulerB).map(j -> {
				System.out.println("nested 2nd map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribe(j -> {
				System.out.println("subscribe():" + Thread.currentThread().getName() + " " + i + " " + j);
			});
		}).subscribe();

		/**
		 1st map():Test worker 1
		 1st map():Test worker 2
		 1st map():Test worker 3

		 publishOn에 의해 스케쥴링, 첫번째 쓰레드가 작업을 시작한다.
		 2nd map():boundedElastic-1 10
		 2nd map():boundedElastic-1 20
		 2nd map():boundedElastic-1 30
		 nested 1st map():boundedElastic-1 10 1
		 nested 1st map():boundedElastic-1 10 2

		 bondedElastic 스케쥴러에게 다시 publish, 두번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-2 10 10
		 nested 2nd map():boundedElastic-2 10 20
		 subscribe():boundedElastic-2 10 100
		 subscribe():boundedElastic-2 10 200

		 nested 1st map():boundedElastic-1 20 1
		 nested 1st map():boundedElastic-1 20 2

		 boundedElastic 스케쥴러에게 다시 publish, 세번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-3 20 10
		 nested 2nd map():boundedElastic-3 20 20
		 subscribe():boundedElastic-3 20 100
		 subscribe():boundedElastic-3 20 200


		 nested 1st map():boundedElastic-1 30 1
		 nested 1st map():boundedElastic-1 30 2

		 boundedElastic 스케쥴러에게 다시 publish, 네번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-4 30 10
		 nested 2nd map():boundedElastic-4 30 20
		 subscribe():boundedElastic-4 30 100
		 subscribe():boundedElastic-4 30 200
		 */
	}

	@Test
	void publishOuterSubscribeInnerWithCustomScheduler() {

		// inner Flux에 대해서 모든 작업은 subscribeOn(schedulerB) 에 의해 schedulerB가 맡게 된다.
		// outer Flux에 대해서는 map1과 map2 사이에 publishOn(schedulerA)에 의해 map2작업부터 schedulerA가 맡게 된다.
		Flux.range(1, 3).map(i -> {
			System.out.println("1st map():" + Thread.currentThread().getName() + " " + i);
			return i * 10;
		}).publishOn(schedulerA).map(i -> {
			System.out.println("2nd map():" + Thread.currentThread().getName() + " " + i);
			return Flux.fromIterable(Arrays.asList(1, 2)).map(j -> {
				System.out.println("nested 1st map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribeOn(schedulerB).map(j -> {
				System.out.println("nested 2nd map():" + Thread.currentThread().getName() + " " + i + " " + j);
				return j * 10;
			}).subscribe(j -> {
				System.out.println("subscribe():" + Thread.currentThread().getName() + " " + i + " " + j);
			});
		}).subscribe();
		/**
		 1st map():Test worker 1
		 1st map():Test worker 2
		 1st map():Test worker 3

		 publishOn에 의해 스케쥴링, 첫번째 쓰레드가 작업을 시작한다.
		 2nd map():boundedElastic-1 10
		 2nd map():boundedElastic-1 20
		 2nd map():boundedElastic-1 30
		 nested 1st map():boundedElastic-1 10 1
		 nested 1st map():boundedElastic-1 10 2

		 bondedElastic 스케쥴러에게 다시 publish, 두번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-2 10 10
		 nested 2nd map():boundedElastic-2 10 20
		 subscribe():boundedElastic-2 10 100
		 subscribe():boundedElastic-2 10 200

		 nested 1st map():boundedElastic-1 20 1
		 nested 1st map():boundedElastic-1 20 2

		 boundedElastic 스케쥴러에게 다시 publish, 세번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-3 20 10
		 nested 2nd map():boundedElastic-3 20 20
		 subscribe():boundedElastic-3 20 100
		 subscribe():boundedElastic-3 20 200


		 nested 1st map():boundedElastic-1 30 1
		 nested 1st map():boundedElastic-1 30 2

		 boundedElastic 스케쥴러에게 다시 publish, 네번째 쓰레드가 작업을 시작한다.

		 nested 2nd map():boundedElastic-4 30 10
		 nested 2nd map():boundedElastic-4 30 20
		 subscribe():boundedElastic-4 30 100
		 subscribe():boundedElastic-4 30 200
		 */
	}

	@Test
	void executionTime1() throws InterruptedException {
		int cases = 4;
		AtomicLong[] befores = new AtomicLong[cases];
		for (int i = 0; i < 4; i++) {
			befores[i] = new AtomicLong();
		}
		int count = 10000000;

		/**
		 * pub sub 활용하지 않으면 194 ms
		 */

		Flux.range(1, count)
			.doOnSubscribe(s -> befores[0].set(System.currentTimeMillis()))
			.map(i -> i * 11)
			.filter(i -> i % 100 == 17)
			// .subscribeOn(Schedulers.boundedElastic())
			.map(i -> i / 100)
			.doOnComplete(() -> {
				long after = System.currentTimeMillis();
				long elaspedTime = after - befores[0].get();
				System.out.println("메인 쓰레드만 작업");
				System.out.println(String.format("쓰레드 : %s ", Thread.currentThread().getName()) + elaspedTime + " 밀리초");
			})
			.subscribe();

		/**
		 * map1 <-> map2 사이 sub 103 ms
		 */

		Flux.range(1, count)
			.doOnSubscribe(s -> befores[1].set(System.currentTimeMillis()))
			.map(i -> i * 11)
			.filter(i -> i % 100 == 17)
			.subscribeOn(Schedulers.boundedElastic())
			.map(i -> i / 100)
			.doOnComplete(() -> {
				long after = System.currentTimeMillis();
				long elaspedTime = after - befores[1].get();
				System.out.println("map1과 map2 사이 sub");
				System.out.println(String.format("쓰레드 : %s ", Thread.currentThread().getName()) + elaspedTime + " 밀리초");
			})
			.subscribe();

		/**
		 * map1 <-> map2 사이 pub 129 ms
		 */

		Flux.range(1, count)
			.doOnSubscribe(s -> befores[2].set(System.currentTimeMillis()))
			.map(i -> i * 11)
			.filter(i -> i % 100 == 17)
			.publishOn(Schedulers.boundedElastic())
			.map(i -> i / 100)
			.doOnComplete(() -> {
				long after = System.currentTimeMillis();
				long elaspedTime = after - befores[2].get();
				System.out.println("map1과 map2 사이 pub");
				System.out.println(String.format("쓰레드 : %s ", Thread.currentThread().getName()) + elaspedTime + " 밀리초");
			})
			.subscribe();

		/**
		 * map1 이전 sub 124ms
		 */

		Flux.range(1, count)
			.doOnSubscribe(s -> befores[3].set(System.currentTimeMillis()))
			.subscribeOn(Schedulers.boundedElastic())
			.map(i -> i * 11)
			.filter(i -> i % 100 == 17)
			.map(i -> i / 100)
			.doOnComplete(() -> {
				long after = System.currentTimeMillis();
				long elaspedTime = after - befores[3].get();
				System.out.println("map1이전 sub");
				System.out.println(String.format("쓰레드 : %s ", Thread.currentThread().getName()) + elaspedTime + " 밀리초");
			})
			.subscribe();

		/**
		 * map1 이전 sub 124ms
		 */

		Flux.range(1, count)
			.doOnSubscribe(s -> befores[3].set(System.currentTimeMillis()))
			.publishOn(Schedulers.boundedElastic())
			.map(i -> i * 11)
			.filter(i -> i % 100 == 17)
			.map(i -> i / 100)
			.doOnComplete(() -> {
				long after = System.currentTimeMillis();
				long elaspedTime = after - befores[3].get();
				System.out.println("map1이전 pub");
				System.out.println(String.format("쓰레드 : %s ", Thread.currentThread().getName()) + elaspedTime + " 밀리초");
			})
			.subscribe();

		Thread.sleep(1000L);
	}

	public static class SchedulerA implements Scheduler {

		private static final AtomicInteger atomicInteger = new AtomicInteger(0);

		@Override
		public Disposable schedule(Runnable task) {
			System.out.println("스케쥴러A 클래스의 schedule 함수 실행 ");
			Thread thread = new Thread(task, "스케쥴러A 에 의해 생성된 " + atomicInteger.getAndIncrement() + " 번째 쓰레드");
			thread.start();
			return () -> {
				thread.interrupt();
			};
		}

		@Override
		public Worker createWorker() {
			return new CustomWorker();
		}

		private static class CustomWorker implements Worker {
			@Override
			public Disposable schedule(Runnable task) {
				log.info("스케쥴러A의 CustomWorker 클래스에서  schedule 함수 실행 ");
				Thread thread = new Thread(task, "스케쥴러A 에 의해 생성된 " + atomicInteger.getAndIncrement() + " 번째 쓰레드");
				thread.start();
				return () -> {
					thread.interrupt();
				};
			}

			@Override
			public void dispose() {
				log.info("WorkerA disposed!");
			}
		}
	}

	public static class SchedulerB implements Scheduler {

		private static final AtomicInteger atomicInteger = new AtomicInteger(0);

		@Override
		public Disposable schedule(Runnable task) {
			System.out.println("스케쥴러B 클래스의 schedule 함수 실행 " + atomicInteger.get() + " 번째로 schedule 함수 호출");
			Thread thread = new Thread(task, "스케쥴러B 에 의해 생성된 " + atomicInteger.getAndIncrement() + " 번째 쓰레드");
			thread.start();
			return () -> {
				thread.interrupt();
			};
		}

		@Override
		public Worker createWorker() {
			return new CustomWorker();
		}

		private static class CustomWorker implements Worker {
			@Override
			public Disposable schedule(Runnable task) {
				log.info("스케쥴러B의 CustomWorker schedule 함수 실행 ");
				Thread thread = new Thread(task, "스케쥴러B 에 의해 생성된 " + atomicInteger.getAndIncrement() + " 번째 쓰레드");
				thread.start();
				return () -> {
					thread.interrupt();
				};
			}

			@Override
			public void dispose() {
				log.info("Worker B Disposed!");
			}
		}
	}
}
