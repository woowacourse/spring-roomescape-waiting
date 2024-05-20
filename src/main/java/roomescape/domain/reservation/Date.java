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
    private LocalDate value;

    public Date(String rawDate) {
        try {
            this.value = LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new RoomescapeException("잘못된 날짜 형식입니다.");
        }
    }

    protected Date() {
    }

    public LocalDate getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Date date = (Date) o;
        return Objects.equals(value, date.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
