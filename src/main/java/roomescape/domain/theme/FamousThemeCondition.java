package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.Objects;

public class FamousThemeCondition {
    private static final long DEFAULT_DAYS = 7;
    private static final long DEFAULT_LIMIT = 10;
    private static final int DEFAULT_GAP_DATE = 1;

    private final Long days;
    private final LocalDate date;
    private final Long limit;

    public FamousThemeCondition(Long days, LocalDate date, Long limit, LocalDate now) {
        Objects.requireNonNull(now);
        this.days = Objects.requireNonNullElse(days, DEFAULT_DAYS);
        this.date = Objects.requireNonNullElse(date, now);
        this.limit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
    }

    public LocalDate startDate() {
        return date.minusDays(days);
    }

    public LocalDate endDate() {
        return date.minusDays(DEFAULT_GAP_DATE);
    }

    public Long getDays() {
        return days;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getLimit() {
        return limit;
    }
}
