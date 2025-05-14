package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.time.domain.ReservationTime;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDateTime {

    @Embedded
    private ReservationDate reservationDate;
    @ManyToOne
    private ReservationTime reservationTime;

    private ReservationDateTime(ReservationDate reservationDate, ReservationTime reservationTime) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
    }

    public static ReservationDateTime create(ReservationDate reservationDate, ReservationTime reservationTime) {
        validatePast(reservationDate, reservationTime);
        return new ReservationDateTime(reservationDate, reservationTime);
    }

    public static ReservationDateTime load(ReservationDate reservationDate, ReservationTime reservationTime) {
        return new ReservationDateTime(reservationDate, reservationTime);
    }

    private static void validatePast(ReservationDate reservationDate, ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate.getDate(), reservationTime.getStartAt());
        LocalDateTime now = LocalDateTime.now();

        if (reservationDateTime.isBefore(now)) {
            throw new PastReservationException();
        }
    }

    public ReservationDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
