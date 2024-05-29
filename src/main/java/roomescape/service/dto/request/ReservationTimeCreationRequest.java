package roomescape.service.dto.request;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record ReservationTimeCreationRequest(LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
