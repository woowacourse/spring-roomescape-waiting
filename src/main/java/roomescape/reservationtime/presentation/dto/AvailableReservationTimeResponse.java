package roomescape.reservationtime.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.application.dto.AvailableReservationTimeQueryResult;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean available
) {
    public static AvailableReservationTimeResponse from(AvailableReservationTimeQueryResult result) {
        return new AvailableReservationTimeResponse(result.id(), result.startAt(), result.available());
    }
}
