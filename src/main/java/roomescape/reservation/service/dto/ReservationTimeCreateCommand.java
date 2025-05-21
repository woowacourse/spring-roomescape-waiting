package roomescape.reservation.service.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeCreateCommand(LocalTime startAt) {

    public ReservationTime convertToReservationTime() {
        return new ReservationTime(this.startAt);
    }
}
