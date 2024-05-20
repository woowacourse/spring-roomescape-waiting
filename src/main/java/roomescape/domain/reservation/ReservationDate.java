package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.DateTimeException;
import java.time.LocalDate;

@Embeddable
public record ReservationDate(
        @Column(name = "date") LocalDate value) {

    public static ReservationDate from(final String date) {
        try {
            return new ReservationDate(LocalDate.parse(date));
        } catch (final DateTimeException exception) {
            throw new IllegalArgumentException(String.format("%s 는 유효하지 않은 값입니다.(EX: 10:00)", date));
        }
    }

    public String asString() {
        return value.toString();
    }

}
