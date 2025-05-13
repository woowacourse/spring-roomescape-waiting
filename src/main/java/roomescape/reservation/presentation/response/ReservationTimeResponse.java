package roomescape.reservation.presentation.response;

import java.time.LocalTime;
import roomescape.reservation.business.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResponse of(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
