package roomescape.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        Long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean isBooked
) {
    public static AvailableReservationTimeResponse from(ReservationTime reservationTime, boolean isBooked) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                isBooked
        );
    }
}
