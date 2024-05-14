package roomescape.time.domain;

import java.time.LocalDate;

public class PopularThemePeriod {

    private static final int POPULAR_THEME_START_DATE_BOUNDARY = 7;
    private static final int POPULAR_THEME_END_DATE_BOUNDARY = 1;

    private final LocalDate startDate;
    private final LocalDate endDate;

    public PopularThemePeriod() {
        LocalDate currentDate = LocalDate.now();
        this.startDate = currentDate.minusDays(POPULAR_THEME_START_DATE_BOUNDARY);
        this.endDate = currentDate.minusDays(POPULAR_THEME_END_DATE_BOUNDARY);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
