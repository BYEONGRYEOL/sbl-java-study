package org.sbl;

import static org.assertj.core.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ProcessBuilderLearnTest {

	@Test
	void ProcessBuilderBlockingCallTest() {
		// start()는 블로킹 호출, 프로세스가 시작되면 그 후의 동작을 진행한다.
		AtomicInteger sumOfPrintNumber1 = new AtomicInteger(0);
		AtomicBoolean isAnyLineRead = new AtomicBoolean(false);

		long startTime = System.currentTimeMillis();
		Mono.fromCallable(() -> printProcessFrom1To(100).start())
			.subscribeOn(Schedulers.boundedElastic())
			.doOnSuccess(process -> {
				new Thread(() -> {
					try {
						InputStream inputStream = process.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						while ((line = reader.readLine()) != null) {
							sumOfPrintNumber1.addAndGet(Integer.parseInt(line));
							isAnyLineRead.set(true);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			})
			.block();

		log.info("Process의 출력 읽기 작업이 시작되자마자 출력해보기");
		log.info("소요 시간 " + (System.currentTimeMillis() - startTime));
		log.info("계산된 출력 숫자의 총합 " + sumOfPrintNumber1.get());
		log.info("한번이라도 process 출력 읽는 작업이 성공했는가" + isAnyLineRead.get());

		assertThat(sumOfPrintNumber1.get()).isEqualTo(0);
		assertThat(isAnyLineRead.get()).isEqualTo(false);

	}

	ProcessBuilder powerShellProcess(Long waitMillisecond) throws InterruptedException {
		Thread.sleep(waitMillisecond);
		ProcessBuilder processBuilder = new ProcessBuilder();
		// processBuilder.inheritIO();
		processBuilder.command("powershell.exe", "echo hello-powershell");
		return processBuilder;
	}

	@Test
	@DisplayName("프로세스빌더로 파워쉘 스크립트를 실행하고 ")
	void printDetectFailedByReadLineIsNullTest() {
		AtomicInteger sumOfPrintNumber = new AtomicInteger(0);
		Mono.fromCallable(() -> printProcessFrom1To(100).start())
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe(process -> {
				new Thread(() -> {
					try {
						InputStream inputStream = process.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						while (true) {
							line = reader.readLine();
							sumOfPrintNumber.addAndGet(Integer.parseInt(line));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			});

		assertThat(sumOfPrintNumber.get()).isEqualTo(0);
	}

	/**
	 * ProcessBuilder로 실행한 파워쉘 스크립트의 input stream을 읽으려면
	 * input stream reader 쓰레드가 스크립트가 실행된 이후에 읽기 시작해야한다.
	 */
	@Test
	void printDetectSuccessTest() throws InterruptedException {
		AtomicInteger sumOfPrintNumber2 = new AtomicInteger(0);

		long startTime = System.currentTimeMillis();
		Mono.fromCallable(() -> printProcessFrom1To(100).start())
			.subscribeOn(Schedulers.boundedElastic())
			.doOnSuccess(process -> {
				new Thread(() -> {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					try {
						InputStream inputStream = process.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						while ((line = reader.readLine()) != null) {
							sumOfPrintNumber2.addAndGet(Integer.parseInt(line));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			})
			.block();

		Thread.sleep(1000L);
		log.info("1초의 대기를 걸고 Process의 출력 읽기 작업 시작");
		log.info("소요 시간 " + (System.currentTimeMillis() - startTime));
		log.info("계산된 출력 숫자의 총합 " + sumOfPrintNumber2.get());
		assertThat(sumOfPrintNumber2.get()).isEqualTo(5050);
	}

	@Test
	void processOnExitTest() throws InterruptedException {
		AtomicInteger multipliesOfSevenCount = new AtomicInteger(0);
		Mono.fromCallable(printProcessFrom1To(100)::start).flatMap(process -> {
			process.onExit().thenAccept((c) -> {
				log.info("프로세스가 종료되어 thenAccept 실행");
			});
			try {
				log.info("프로세스 출력 읽기 시작");
				InputStream inputStream = process.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					if (Integer.parseInt(line) % 7 == 0) {
						multipliesOfSevenCount.incrementAndGet();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return Mono.just(multipliesOfSevenCount.get());
		}).log().subscribeOn(Schedulers.boundedElastic()).subscribe();
		Thread.sleep(1000L);
		log.info("sevenCount.get() : " + multipliesOfSevenCount.get());
		assertThat(multipliesOfSevenCount.get()).isEqualTo(100 / 7);
	}

	@Test
	void processBlockingTest() {
		Mono.fromCallable(() -> printProcessFrom1To(100).start()).flatMap(process -> {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			int sevenCount = 0;
			while (true) {
				try {
					line = reader.readLine();
					if (line.contains("7")) {
						sevenCount++;
						System.out.println("탐색 : " + line);
					}
				} catch (IOException e) {
					System.out.println("e = " + e);
				}
			}
		}).subscribeOn(Schedulers.boundedElastic());
	}

	ProcessBuilder printProcessFrom1To(int excludeEnd) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		StringBuilder commands = new StringBuilder();
		for (int i = 1; i <= excludeEnd; i++) {
			commands.append("echo ").append(i).append('\n');
		}
		processBuilder.command("powershell.exe", commands.toString());
		log.info("프로세스빌더 생성");
		return processBuilder;
	}
}
