package roomescape.application.service.result;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResult(long id, LocalTime startAt, String status) {

    public static ReservationTimeResult from(ReservationTime reservationTime) {
        return new ReservationTimeResult(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservationTime.getStatus().toString()
        );
    }
}
