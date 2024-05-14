package roomescape.service.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record ReservationTimeResponse(
        long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startAt) {
    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}
