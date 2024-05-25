package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record FindReservationTimeResponse(Long id, LocalTime startAt) {
    public static FindReservationTimeResponse from(ReservationTime reservationTime) {
        return new FindReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt());
    }
}
