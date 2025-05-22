package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservation.exception.PastReservationException;
import roomescape.time.domain.ReservationTime;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationDateTime {
    @Embedded
    private ReservationDate reservationDate;
    @JoinColumn(name = "time_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    public ReservationDateTime(
            ReservationDate reservationDate,

            ReservationTime reservationTime
    ) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
    }

    public static ReservationDateTime create(ReservationDate reservationDate, ReservationTime reservationTime) {
        validatePast(reservationDate, reservationTime);
        return new ReservationDateTime(reservationDate, reservationTime);
    }

    private static void validatePast(ReservationDate reservationDate, ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate.getDate(), reservationTime.getStartAt());
        LocalDateTime now = LocalDateTime.now();

        if (reservationDateTime.isBefore(now)) {
            throw new PastReservationException();
        }
    }

    public LocalDate getDate() {
        return reservationDate.getDate();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public Long getTimeId() {
        return reservationTime.getId();
    }
}
