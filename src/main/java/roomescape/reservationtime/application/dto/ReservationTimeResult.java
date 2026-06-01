package roomescape.reservationtime.application.dto;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeResult(Long id, LocalTime startAt) {

    public static ReservationTimeResult from(ReservationTime time) {
        return new ReservationTimeResult(
                time.getId(),
                time.getStartAt()
        );
    }

    public static ReservationTimeResult from(Long id, LocalTime startAt) {
        return new ReservationTimeResult(
                id,
                startAt
        );
    }
}
