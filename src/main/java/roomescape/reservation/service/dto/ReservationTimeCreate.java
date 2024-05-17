package roomescape.reservation.service.dto;

import java.time.LocalTime;
import roomescape.reservation.controller.dto.ReservationTimeRequest;

public record ReservationTimeCreate(LocalTime startAt) {
    public static ReservationTimeCreate from(ReservationTimeRequest reservationTimeRequest) {
        return new ReservationTimeCreate(reservationTimeRequest.startAt());
    }
}
