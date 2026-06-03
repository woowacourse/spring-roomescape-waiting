package roomescape.reservationtime.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean available
) {
    public static AvailableReservationTimeResponse from(AvailableReservationTime time) {
        return new AvailableReservationTimeResponse(time.id(), time.startAt(), time.available());
    }
}
