package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean isBooked
) {
    public static AvailableReservationTimeResponse of(final ReservationTime reservationTime, final boolean isBooked) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                isBooked
        );
    }
}
