package roomescape.service.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationAvailabilityTimeResponse(
        @NotNull
        Long id,
        @NotNull
        LocalTime startAt,
        boolean booked) {

    public static ReservationAvailabilityTimeResponse from(ReservationTime reservationTime, boolean booked) {
        return new ReservationAvailabilityTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                booked);
    }
}
