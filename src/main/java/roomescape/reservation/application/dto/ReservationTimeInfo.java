package roomescape.reservation.application.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.time.ReservationTime;

public record ReservationTimeInfo(Long id, LocalTime startAt) {

    public ReservationTimeInfo(final ReservationTime reservationTime) {
        this(reservationTime.id(), reservationTime.startAt());
    }
}
