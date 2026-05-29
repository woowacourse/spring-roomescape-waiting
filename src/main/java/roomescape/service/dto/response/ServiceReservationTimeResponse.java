package roomescape.service.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ServiceReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ServiceReservationTimeResponse from(ReservationTime reservationTime) {
        return new ServiceReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
