package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record FindTimeOfReservationsResponse(Long id, LocalTime startAt) {
    public static FindTimeOfReservationsResponse from(ReservationTime reservationTime) {
        return new FindTimeOfReservationsResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
