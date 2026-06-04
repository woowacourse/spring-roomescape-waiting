package roomescape.reservation.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeAvailabilityResponse(
    Long timeId,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt,
    boolean available
) {

    public static ReservationTimeAvailabilityResponse of(ReservationTime reservationTime, boolean available) {
        return new ReservationTimeAvailabilityResponse(
            reservationTime.getId(),
            reservationTime.getStartAt(),
            available
        );
    }
}
