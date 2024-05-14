package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record TimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static TimeResponse toResponse(ReservationTime reservationTime) {
        return new TimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
