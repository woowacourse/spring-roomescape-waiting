package roomescape.reservation.dto.request;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
