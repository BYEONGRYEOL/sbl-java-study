package org.sbl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class TimerAop {

	@Pointcut("@annotation(org.sbl.Timer)")
	void cut() {
	}

	@Around("cut()")
	public void measure(ProceedingJoinPoint joinPoint) throws Throwable {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		joinPoint.proceed();
		stopWatch.stop();

		String methodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getName();
		System.out.printf("\n %s : %d ms \n", methodName, stopWatch.getTotalTimeMillis());
	}
}
