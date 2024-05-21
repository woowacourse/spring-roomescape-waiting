package roomescape.domain.theme;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Objects;

public class ThemePopularFilter {

    private static final int POPULARITY_AGGREGATION_PERIOD = 7;
    private static final int POPULARITY_AGGREGATION_LIMIT = 10;

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int limit;

    private ThemePopularFilter(final LocalDate startDate, final LocalDate endDate, final int limit) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.limit = limit;
    }

    public static ThemePopularFilter from(final LocalDate nowDate) {
        final LocalDate startDate = nowDate.minusDays(POPULARITY_AGGREGATION_PERIOD);
        return new ThemePopularFilter(startDate, nowDate, POPULARITY_AGGREGATION_LIMIT);
    }

    public Pageable ofSize() {
        return PageRequest.ofSize(limit);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePopularFilter that = (ThemePopularFilter) o;
        return limit == that.limit && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, limit);
    }
}
