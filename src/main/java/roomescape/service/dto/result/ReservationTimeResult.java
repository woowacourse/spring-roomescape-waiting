package roomescape.service.dto.result;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt,
        LocalTime endAt
) {

    public static ReservationTimeResult from(final ReservationTime reservationTime) {
        return new ReservationTimeResult(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservationTime.getEndAt()
        );
    }
}
