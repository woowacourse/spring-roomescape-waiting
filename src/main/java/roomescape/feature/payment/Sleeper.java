package roomescape.feature.payment;

import java.time.Duration;

/**
 * 재시도 전 대기를 추상화한다. 실제 구현은 {@code Thread.sleep} 이지만,
 * 테스트는 가짜 구현으로 실제로 자지 않으면서 "얼마를 기다렸는가"만 결정적으로 검증한다.
 * ({@code NanoClock} 과 동일한 철학)
 */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration);
}
