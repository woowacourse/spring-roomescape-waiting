package roomescape.domain.reservation.dto;

import java.time.LocalTime;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

public record ReservationTimeAddRequest(LocalTime startAt) {
    public ReservationTime toEntity() {
        return new ReservationTime(null, startAt);
    }
}
