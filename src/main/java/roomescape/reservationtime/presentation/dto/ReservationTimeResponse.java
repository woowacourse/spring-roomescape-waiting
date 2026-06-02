package roomescape.reservationtime.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;

public record ReservationTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTimeQueryResult result) {
        return new ReservationTimeResponse(result.id(), result.startAt());
    }
}
