package roomescape.reservation.time.presentation.dto;

import java.time.LocalTime;
import roomescape.reservation.time.domain.ReservationTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {
    public static ReservationTimeResponse from(final ReservationTime findReservationTime) {
        return new ReservationTimeResponse(findReservationTime.getId(), findReservationTime.getStartAt());
    }
}
