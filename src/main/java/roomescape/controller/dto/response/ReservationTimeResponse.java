package roomescape.controller.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.ReservationTimeInfo;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }

    public static ReservationTimeResponse from(ReservationTimeInfo reservationTimeInfo) {
        return new ReservationTimeResponse(
                reservationTimeInfo.id(),
                reservationTimeInfo.startAt()
        );
    }
}
