package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.time.domain.ReservationTime;

@Embeddable
public record ReservationDateTime(
        @Embedded
        ReservationDate reservationDate,

        @JoinColumn(name = "time_id")
        @ManyToOne
        ReservationTime reservationTime
) {

    public static ReservationDateTime create(ReservationDate reservationDate, ReservationTime reservationTime) {
        validatePast(reservationDate, reservationTime);
        return new ReservationDateTime(reservationDate, reservationTime);
    }

    private static void validatePast(ReservationDate reservationDate, ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate.date(), reservationTime.getStartAt());
        LocalDateTime now = LocalDateTime.now();

        if (reservationDateTime.isBefore(now)) {
            throw new PastReservationException();
        }
    }
}
