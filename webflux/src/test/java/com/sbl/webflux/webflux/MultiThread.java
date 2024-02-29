package com.sbl.webflux.webflux;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class MultiThread {
	@Test
	void cputask() throws InterruptedException {
		final long N = 1000000000L; // 1억
		final int maxNumThreads = 100; // 실험에 사용할 최대 스레드 수

		long[] executionTimes = new long[maxNumThreads];

		for (int numThreads = 1; numThreads <= maxNumThreads; numThreads++) {
			Thread[] threads = new Thread[numThreads];

			long before = System.currentTimeMillis();
			long chunkSize = N / numThreads;
			for (int i = 0; i < numThreads; i++) {
				threads[i] = new Thread(() -> {
					int t = 0;
					for (int j = 1; j <= chunkSize; j++) {
						t += j;
					}
				});
				threads[i].start();
			}

			for (Thread thread : threads) {
				thread.join();
			}

			long after = System.currentTimeMillis();
			executionTimes[numThreads - 1] = after - before;
		}

		// 여기서부터는 엑셀에 결과를 쓰는 코드입니다.
		// 이 코드는 엑셀을 직접 조작하지 않고 CSV 파일로 저장합니다.

		try {
			FileWriter csvWriter = new FileWriter("./execution_times1billion.csv");
			csvWriter.append("NumThreads,ExecutionTime(ms)\n");
			for (int i = 0; i < maxNumThreads; i++) {
				csvWriter.append((i + 1) + "," + executionTimes[i] + "\n");
			}

			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void iotask() throws IOException, InterruptedException {
		final long N = 100000000; // 1만줄
		final int maxNumThreads = 100; // 실험에 사용할 최대 스레드 수
		long[] executionTimes = new long[maxNumThreads];
		for (int numThreads = 1; numThreads <= maxNumThreads; numThreads++) {
			Thread[] threads = new Thread[numThreads];
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter("temp" + numThreads + ".txt"));
			long before = System.currentTimeMillis();
			long chunkSize = N / numThreads;
			for (int i = 0; i < numThreads; i++) {
				threads[i] = new Thread(() -> {
					for (long j = 0; j < chunkSize; j++) {
						try {
							fileWriter.write(UUID.randomUUID().toString());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					try {
						fileWriter.flush();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				threads[i].start();
			}

			for (Thread thread : threads) {
				thread.join();
			}

			long after = System.currentTimeMillis();
			executionTimes[numThreads - 1] = after - before;
		}

		// 여기서부터는 엑셀에 결과를 쓰는 코드입니다.
		// 이 코드는 엑셀을 직접 조작하지 않고 CSV 파일로 저장합니다.

		try {
			FileWriter csvWriter = new FileWriter("./execution_times_io.csv");
			csvWriter.append("NumThreads,ExecutionTime(ms)\n");
			for (int i = 0; i < maxNumThreads; i++) {
				csvWriter.append((i + 1) + "," + executionTimes[i] + "\n");
			}

			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


