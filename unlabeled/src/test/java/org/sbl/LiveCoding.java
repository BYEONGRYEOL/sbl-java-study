package org.sbl;

import org.junit.jupiter.api.Test;

public class LiveCoding {
	@Test
	void reverseArrayThroughMakeNewArray() {
		// 테스트 빌드 실패시 oom 에러일 확률이 높음
		// java -Xmx2G -Xms2G -jar your-application.jar
		int len = 50000000;
		int[] array = new int[len];
		for (int i = 0; i < len; i++) {
			array[i] = i;
		}
		int[] newArray = new int[len];
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < len; i++) {
			newArray[i] = len - i;
		}
		long end = System.currentTimeMillis();
		System.out.println("새로운 배열 생성" + (end - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < len / 2; i++) {
			int temp = array[i];
			array[i] = array[len - 1 - i];
			array[len - 1 - i] = temp;
		}
		end = System.currentTimeMillis();
		System.out.println("temp 활용 뒤집기" + (end - start));
	}
}
