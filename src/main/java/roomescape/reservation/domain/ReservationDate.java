package roomescape.reservation.domain;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ReservationDate {

    private final Long id;
    private final LocalDate date;

    public ReservationDate(Long id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public static ReservationDate of(long dateId, LocalDate date) {
        return new ReservationDate(dateId, date);
    }

    public static ReservationDate createWithoutId(LocalDate reservationDate) {
        return new ReservationDate(
            null,
            reservationDate
        );
    }

    public boolean isBefore(LocalDate compareDate) {
        return date.isBefore(compareDate);
    }

    public boolean isSame(LocalDate compareDate) {
        return date.isEqual(compareDate);
    }
}
