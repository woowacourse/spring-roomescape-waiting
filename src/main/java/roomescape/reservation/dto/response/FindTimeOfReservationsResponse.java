package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record FindTimeOfReservationsResponse(Long id, LocalTime startAt) {
    public static FindTimeOfReservationsResponse from(final ReservationTime reservationTime) {
        return new FindTimeOfReservationsResponse(
                reservationTime.getId(),
                reservationTime.getTime()
        );
    }
}
