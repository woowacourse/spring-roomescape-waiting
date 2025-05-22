package roomescape.reservation.application.time.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.time.ReservationTime;

public record ReservationTimeCreateCommand(LocalTime startAt) {

    public ReservationTime convertToEntity() {
        return new ReservationTime(startAt);
    }
}
