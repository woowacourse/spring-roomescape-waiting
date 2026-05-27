package roomescape.domain.policy;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class RecentWeekPopularPolicy implements PopularThemePolicy {

    private static final int PERIOD_DAYS = 7;
    private static final int LIMIT = 10;

    private final Clock clock;

    public RecentWeekPopularPolicy() {
        this.clock = Clock.systemDefaultZone();
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
