package roomescape.reservation.dto;

import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record AvailableReservationTimeResponse(
        Long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeResponse of(
            final ReservationTime reservationTime,
            final boolean alreadyBooked
    ) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked);
    }
}
