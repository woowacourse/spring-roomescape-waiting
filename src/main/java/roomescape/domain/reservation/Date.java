package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import roomescape.global.exception.RoomescapeException;

public class Date {

    private final LocalDate date;

    public Date(String rawDate) {
        try {
            this.date = LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new RoomescapeException("잘못된 날짜 형식입니다.");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Date date1 = (Date) o;
        return Objects.equals(date, date1.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
