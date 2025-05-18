package roomescape.reservation.application.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.time.ReservationTime;

public record ReservationTimeCreateCommand(LocalTime startAt) {

    public ReservationTime convertToReservationTime() {
        return new ReservationTime(this.startAt);
    }
}
