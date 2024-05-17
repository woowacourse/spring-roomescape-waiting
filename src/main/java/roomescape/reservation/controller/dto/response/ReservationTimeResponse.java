package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeResponse(
        long id,

        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt
) {
    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
