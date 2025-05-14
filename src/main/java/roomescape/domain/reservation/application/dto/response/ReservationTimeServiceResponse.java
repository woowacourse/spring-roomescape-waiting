package roomescape.domain.reservation.application.dto.response;

import java.time.LocalTime;
import roomescape.domain.reservation.model.entity.ReservationTime;

public record ReservationTimeServiceResponse(
        Long id,
        LocalTime startAt,
        boolean isBooked
) {

    public static ReservationTimeServiceResponse of(ReservationTime reservationTime, boolean isBooked) {
        return new ReservationTimeServiceResponse(reservationTime.getId(), reservationTime.getStartAt(), isBooked);
    }

    public static ReservationTimeServiceResponse withoutBook(ReservationTime reservationTime) {
        return new ReservationTimeServiceResponse(reservationTime.getId(), reservationTime.getStartAt(), false);
    }
}
