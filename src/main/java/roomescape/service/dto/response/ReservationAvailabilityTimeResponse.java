package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationAvailabilityTimeResponse(
        @NotNull
        Long id,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean booked)
{
    public static ReservationAvailabilityTimeResponse from(ReservationTime reservationTime, boolean booked) {
        return new ReservationAvailabilityTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                booked);
    }
}
