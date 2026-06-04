package roomescape.service.dto.result;

import java.time.LocalTime;
import roomescape.domain.slot.time.ReservationTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResult from(ReservationTime reservationTime) {
        return new ReservationTimeResult(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
