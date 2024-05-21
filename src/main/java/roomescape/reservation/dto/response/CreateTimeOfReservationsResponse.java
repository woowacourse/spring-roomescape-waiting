package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record CreateTimeOfReservationsResponse(Long id, LocalTime startAt) {
    public static CreateTimeOfReservationsResponse from(final ReservationTime reservationTime) {
        return new CreateTimeOfReservationsResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
