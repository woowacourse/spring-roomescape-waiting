package roomescape.reservationtime.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record FindReservationTimeResponse(Long id, LocalTime startAt) {
    public static FindReservationTimeResponse from(final ReservationTime reservationTime) {
        return new FindReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getTime());
    }
}
