package roomescape.application.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.PopularThemeLookupFilter;

public class WeeklyPopularThemeFilter implements PopularThemeLookupFilter {
    private static final int START_DATE_OFFSET = 7;
    private static final int END_DATE_OFFSET = 1;
    private static final int LIMIT_COUNT = 10;

    private final LocalDate currentDate;

    public WeeklyPopularThemeFilter(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public LocalDate startDate() {
        return currentDate.minusDays(START_DATE_OFFSET);
    }

    @Override
    public LocalDate endDate() {
        return currentDate.minusDays(END_DATE_OFFSET);
    }

    @Override
    public int limitCount() {
        return LIMIT_COUNT;
    }
}
