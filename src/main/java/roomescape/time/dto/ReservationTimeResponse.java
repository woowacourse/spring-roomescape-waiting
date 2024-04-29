package roomescape.time.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {

    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}
