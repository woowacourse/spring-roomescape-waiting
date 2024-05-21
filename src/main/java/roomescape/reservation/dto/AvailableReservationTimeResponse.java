package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean booked
) {

    public AvailableReservationTimeResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getId(), reservationTime.getStartAt(), alreadyBooked);
    }
}
