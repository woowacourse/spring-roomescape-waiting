package roomescape.domain.theme;

import java.time.Clock;
import java.time.LocalDate;

public class DateRange {

    private static final int DAYS_BEFORE_A_WEEK = 7;
    private static final int DAYS_BEFORE_YESTERDAY = 1;

    private final LocalDate start;
    private final LocalDate end;

    private DateRange(final LocalDate start, final LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public static DateRange createLastWeekRange(final Clock clock) {
        validateClock(clock);
        LocalDate now = LocalDate.now(clock);
        return new DateRange(now.minusDays(DAYS_BEFORE_A_WEEK), now.minusDays(DAYS_BEFORE_YESTERDAY));
    }

    private static void validateClock(final Clock clock) {
        if (clock == null) {
            throw new NullPointerException("Clock은 null일 수 없습니다.");
        }
    }

    public LocalDate getStartDate() {
        return start;
    }

    public LocalDate getEndDate() {
        return end;
    }
}
