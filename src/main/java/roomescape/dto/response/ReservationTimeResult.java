package roomescape.dto.response;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt,
        LocalTime endAt
) {

    public static ReservationTimeResult from(ReservationTime reservationTime) {
        return new ReservationTimeResult(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservationTime.getEndAt()
        );
    }
}
