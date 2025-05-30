package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean isBooked
) {

    public static ReservationTimeResponse from(ReservationTimeServiceResponse response) {
        return new ReservationTimeResponse(response.id(), response.startAt(), response.isBooked());
    }
}
