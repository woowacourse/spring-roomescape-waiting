package roomescape.feature.payment;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 실제로 자지 않고 요청받은 대기 시간만 기록하는 테스트용 {@link Sleeper}.
 * Retry-After 재시도를 실시간 대기 없이 결정적으로 검증한다.
 */
public class FakeSleeper implements Sleeper {

    private final List<Duration> sleptDurations = new ArrayList<>();

    @Override
    public void sleep(Duration duration) {
        sleptDurations.add(duration);
    }

    public List<Duration> sleptDurations() {
        return sleptDurations;
    }
}
