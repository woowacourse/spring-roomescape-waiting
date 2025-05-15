package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.entity.ReservationTime;

public record ReservationTimeCreateResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public static ReservationTimeCreateResponse from(ReservationTime reservationTime) {
        return new ReservationTimeCreateResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
