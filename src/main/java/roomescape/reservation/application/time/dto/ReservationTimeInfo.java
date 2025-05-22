package roomescape.reservation.application.time.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.time.ReservationTime;

public record ReservationTimeInfo(long id, LocalTime startAt) {

    public ReservationTimeInfo(final ReservationTime reservationTime) {
        this(reservationTime.id(), reservationTime.startAt());
    }
}
