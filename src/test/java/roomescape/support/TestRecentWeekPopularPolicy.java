package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import roomescape.domain.policy.PopularThemePolicy;

/**
 * 운영 RecentWeekPopularPolicy와 동일한 규칙(최근 7일, 최대 10개)을 갖되, 시계만 주입받는 버전.
 * FixedPopularPolicyConfig가 고정 Clock과 함께 사용한다.
 */
public class TestRecentWeekPopularPolicy implements PopularThemePolicy {

    private static final int PERIOD_DAYS = 7;
    private static final int LIMIT = 10;

    private final Clock clock;

    public TestRecentWeekPopularPolicy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @Override
    public LocalDate from(LocalDate today) {
        return today.minusDays(PERIOD_DAYS);
    }

    @Override
    public LocalDate to(LocalDate today) {
        return today;
    }

    @Override
    public int limit() {
        return LIMIT;
    }
}
