package roomescape.reservationtime.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record CreateReservationTimeResponse(Long id, LocalTime startAt) {
    public static CreateReservationTimeResponse from(final ReservationTime reservationTime) {
        return new CreateReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt());
    }
}
