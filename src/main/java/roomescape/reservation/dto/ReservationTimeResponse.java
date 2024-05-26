package roomescape.reservation.dto;

import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ReservationTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
