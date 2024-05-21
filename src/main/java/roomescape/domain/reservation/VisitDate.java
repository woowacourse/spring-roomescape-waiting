package roomescape.domain.reservation;

import java.time.DateTimeException;
import java.time.LocalDate;

public record VisitDate(LocalDate date) {

    public static VisitDate from(final String date) {
        try {
            return new VisitDate(LocalDate.parse(date));
        } catch (final DateTimeException exception) {
            throw new IllegalArgumentException(String.format("%s 는 유효하지 않은 값입니다.(EX: 10:00)", date));
        }
    }

    public VisitDate beforeDay() {
        return new VisitDate(this.date.minusDays(1));
    }

    public VisitDate beforeWeek() {
        return new VisitDate(this.date.minusWeeks(1));
    }

    public String asString() {
        return date.toString();
    }
}
