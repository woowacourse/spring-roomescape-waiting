package roomescape.member.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record CreateTimeOfReservationResponse(Long id,
                                              LocalTime startAt) {
    public static CreateTimeOfReservationResponse from(final ReservationTime reservationTime) {
        return new CreateTimeOfReservationResponse(
                reservationTime.getId(),
                reservationTime.getTime());
    }
}
