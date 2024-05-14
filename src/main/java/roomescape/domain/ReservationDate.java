package roomescape.domain;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ReservationDate {

    private final LocalDate date;

    public ReservationDate(String date) {
        this.date = parseDate(date);
    }

    private LocalDate parseDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식을 입력하셨습니다.");
        }
    }

    public boolean isBeforeNow() {
        return date.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return date.isEqual(LocalDate.now());
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationDate that = (ReservationDate) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
