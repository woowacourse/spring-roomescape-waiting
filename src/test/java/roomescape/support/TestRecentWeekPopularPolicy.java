package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import roomescape.domain.policy.PopularThemePolicy;

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
