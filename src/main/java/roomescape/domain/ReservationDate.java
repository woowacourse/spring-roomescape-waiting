package roomescape.domain;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Embeddable
public class ReservationDate {

    private LocalDate date;

    public ReservationDate() {
    }

    public ReservationDate(String date) {
        this(parseDate(date));
    }

    public ReservationDate(LocalDate date) {
        this.date = date;
    }

    private static LocalDate parseDate(String rawDate) {
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
