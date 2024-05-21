package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record BookingStatusResponse(@JsonFormat(pattern = "HH:mm") LocalTime startAt, Long timeId, boolean alreadyBooked) {

    public BookingStatusResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
