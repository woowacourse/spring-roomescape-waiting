package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

@Getter
public class FamousThemeCondition {
    private static final long DEFAULT_DAYS = 7;
    private static final long DEFAULT_LIMIT = 10;
    private static final int DEFAULT_GAP_DATE = 1;

    private final Long recentDays;
    private final LocalDate baseDate;
    private final Long limit;

    public FamousThemeCondition(Long recentDays, LocalDate baseDate, Long limit, LocalDate now) {
        Objects.requireNonNull(now);
        this.recentDays = Objects.requireNonNullElse(recentDays, DEFAULT_DAYS);
        this.baseDate = Objects.requireNonNullElse(baseDate, now);
        this.limit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
    }

    public LocalDate startDate() {
        return baseDate.minusDays(recentDays);
    }

    public LocalDate endDate() {
        return baseDate.minusDays(DEFAULT_GAP_DATE);
    }
}
