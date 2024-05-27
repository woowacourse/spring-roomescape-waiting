package roomescape.reservation.domain;

import java.time.LocalDate;

public enum Period {
    DAY(LocalDate.now().minusDays(1) , LocalDate.now()),
    WEEK(LocalDate.now().minusWeeks(1), LocalDate.now()),
    MONTH(LocalDate.now().minusMonths(1), LocalDate.now()),
    SIX_MONTH(LocalDate.now().minusMonths(6), LocalDate.now()),
    YEAR(LocalDate.now().minusYears(1), LocalDate.now()),
    ;

    private final LocalDate startDate;
    private final LocalDate endDate;

    Period(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
