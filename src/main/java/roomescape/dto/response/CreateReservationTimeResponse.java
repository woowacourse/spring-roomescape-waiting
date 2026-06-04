package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record CreateReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static CreateReservationTimeResponse from(ReservationTime reservationTime) {
        return new CreateReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
