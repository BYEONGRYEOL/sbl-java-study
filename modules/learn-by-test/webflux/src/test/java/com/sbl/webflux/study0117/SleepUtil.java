package com.sbl.webflux.study0117;

public class SleepUtil {
	static public void sleepFor(int sec) {
		try {
			Thread.sleep(sec * 1000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
