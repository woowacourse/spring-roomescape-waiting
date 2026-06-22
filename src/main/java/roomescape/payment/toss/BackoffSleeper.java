package roomescape.payment.toss;

/**
 * 재시도 백오프 대기 추상화. 운영은 Thread.sleep으로 실제 대기하고, 테스트는 즉시 반환하는 가짜를 주입해
 * 대기 없이 재시도 흐름을 결정적으로 검증한다(System.sleep을 박지 않는 이유 = 토큰 버킷의 가짜 시계와 같은 결).
 */
@FunctionalInterface
public interface BackoffSleeper {

    void sleep(long millis);

    static BackoffSleeper realTime() {
        return millis -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("재시도 대기 중 인터럽트되었습니다.", e);
            }
        };
    }
}
