package roomescape.reservation.service.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeInfo(Long id, LocalTime startAt) {

    public ReservationTimeInfo(final ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}
