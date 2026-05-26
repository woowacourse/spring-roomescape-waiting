package roomescape.reservationtime.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt,
        boolean isNotReserved
) {
    public static ReservationTimeResponse from(ReservationTime reservationTime, boolean isNotReserved) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                isNotReserved);
    }
}
