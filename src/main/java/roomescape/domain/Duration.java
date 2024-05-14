package roomescape.domain;

import java.time.LocalDate;

public class Duration {
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Duration(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Duration ofLastWeek() {
        return Duration.ofLastWeek(LocalDate.now());
    }

    public static Duration ofLastWeek(LocalDate baseDate) {
        return new Duration(
                baseDate.minusDays(7),
                baseDate.minusDays(1)
        );
    }

    public boolean contains(LocalDate date) {
        return !this.startDate.isAfter(date)
                && !this.endDate.isBefore(date);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
