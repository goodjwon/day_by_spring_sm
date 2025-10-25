package com.example.jvm;

public class LongOverflowDemo {
    public static void main(String[] args) {
        System.out.println("=== Java Long 타입 범위 테스트 ===\n");

        // 1. Long 타입의 최댓값과 최솟값
        demonstrateRanges();

        // 2. 오버플로우 시뮬레이션
        demonstrateOverflow();

        // 3. 10초 실측 기반 계산
        demonstrateFastIncrement();

        // 4. Integer와 비교
        compareWithInteger();
    }

    /**
     * Long 타입의 범위 출력
     */
    public static void demonstrateRanges() {
        System.out.println("[ 1단계: Long 범위 확인 ]");
        System.out.println("Long.MIN_VALUE: " + Long.MIN_VALUE);
        System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE);
        System.out.println("범위: " + Long.MAX_VALUE + " ~ " + Long.MIN_VALUE);
        System.out.println("총 범위: " + (Long.MAX_VALUE - Long.MIN_VALUE) + "\n");
    }

    /**
     * 오버플로우가 발생하는 지점을 직접 보여줌
     */
    public static void demonstrateOverflow() {
        System.out.println("[ 2단계: 오버플로우 증명 ]");

        // Long.MAX_VALUE 근처에서 시작
        long value = Long.MAX_VALUE - 5;

        System.out.println("Long.MAX_VALUE - 5부터 시작하여 증가시킴:");
        for (int i = 0; i <= 10; i++) {
            System.out.println("  " + i + ": " + value);
            value++;
        }

        System.out.println("\n→ Long.MAX_VALUE를 넘으면 Long.MIN_VALUE로 돌아옴!\n");
    }

    /**
     * 실제로 0부터 Long.MAX_VALUE까지 가려면 얼마나 걸리는가?
     * (계산 없음 - 실측 데이터만 사용)
     */
    public static void calculateTimeToMaxValue() {
        // 이 단계는 생략 - 실측 데이터(10초)로만 계산함
    }

    /**
     * 실시간으로 증가시켜보는 (10초 측정, 1초마다 출력)
     * 1초마다 진행 상황을 표시
     */
    public static void demonstrateFastIncrement() {
        System.out.println("[ 3단계: 10초 동안 1초마다 증가 속도 측정 ]\n");

        long value = 0;
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        int secondCounter = 0;

        while (System.currentTimeMillis() - startTime < 10000) {
            value++;

            // 1초마다 확인
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= 1000) {
                secondCounter++;
                long elapsedSeconds = (currentTime - startTime) / 1000;
                double incrementPerSecond = value / (elapsedSeconds > 0 ? (double)elapsedSeconds : 1);
                long yearsNeeded = (long) (Long.MAX_VALUE / incrementPerSecond / (24 * 60 * 60) / 365);

                System.out.printf("%2d초: %,15d  (추정: %,6d년)\n",
                        secondCounter, value, yearsNeeded);

                lastTime = currentTime;
            }
        }

        // 최종 결과
        long totalTime = System.currentTimeMillis() - startTime;
        double finalIncrementPerSecond = value / (totalTime / 1000.0);
        long finalYearsNeeded = (long) (Long.MAX_VALUE / finalIncrementPerSecond / (24 * 60 * 60) / 365);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("최종 결과:");
        System.out.println("10초 동안 도달한 값: " + String.format("%,d", value));
        System.out.println("평균 속도: " + String.format("%.0f", finalIncrementPerSecond) + " 증가/초");
        System.out.println("Long.MAX_VALUE까지 필요한 시간: " + String.format("%,d", finalYearsNeeded) + "년");
        System.out.println("=".repeat(50));
    }

    /**
     * Integer 타입과 비교
     */
    public static void compareWithInteger() {
        System.out.println("[ 5단계: Integer vs Long 비교 ]");

        System.out.println("Integer 범위:");
        System.out.println("  MIN: " + Integer.MIN_VALUE);
        System.out.println("  MAX: " + Integer.MAX_VALUE);
        System.out.println("  크기: " + (Integer.MAX_VALUE - Integer.MIN_VALUE) + "\n");

        System.out.println("Long 범위:");
        System.out.println("  MIN: " + Long.MIN_VALUE);
        System.out.println("  MAX: " + Long.MAX_VALUE);
        System.out.println("  크기: " + (Long.MAX_VALUE - Long.MIN_VALUE) + "\n");

        // Long이 Integer의 몇배인가?
        double ratio = (double) Long.MAX_VALUE / Integer.MAX_VALUE;
        System.out.println("Long이 Integer보다 약 " + String.format("%.0f", ratio) + "배 큼");
    }
}
