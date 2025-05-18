package roomescape.reservationTime.dto.response;

import java.time.LocalTime;
import roomescape.reservationTime.domain.ReservationTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {
    public static ReservationTimeResponse from(final ReservationTime findReservationTime) {
        return new ReservationTimeResponse(findReservationTime.getId(), findReservationTime.getStartAt());
    }
}
