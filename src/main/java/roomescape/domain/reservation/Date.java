package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import roomescape.global.exception.RoomescapeException;

@Embeddable
public class Date {

    @Column(name = "date")
    private LocalDate date;

    public Date(String rawDate) {
        try {
            this.date = LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new RoomescapeException("잘못된 날짜 형식입니다.");
        }
    }

    protected Date() {
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
