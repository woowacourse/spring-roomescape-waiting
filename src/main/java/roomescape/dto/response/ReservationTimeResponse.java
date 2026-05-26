package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.result.ReservationTimeResult;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResponse from(ReservationTimeResult result) {
        return new ReservationTimeResponse(
                result.id(),
                result.startAt()
        );
    }

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
