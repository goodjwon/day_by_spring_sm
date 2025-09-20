package com.example.patten.proxy.after;

// PerformanceAspect.java
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    // "com.example.service" 패키지 하위의 모든 메서드에 적용
    @Around("execution(* com.example.patten.proxy.after..*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 프록시가 실제 메서드를 호출하는 부분
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("✨ [AOP 프록시] " + joinPoint.getSignature().toShortString()
                + " 실행 시간: " + duration + "ms");
        return result;
    }
}
