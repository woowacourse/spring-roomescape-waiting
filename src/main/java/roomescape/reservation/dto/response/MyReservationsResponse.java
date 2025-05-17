package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.Reservation;

public record MyReservationsResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MyReservationsResponse from(Reservation reservation) {
        return new MyReservationsResponse(
                reservation.getId(),
                reservation.themeName(),
                reservation.getDate(),
                reservation.startTime(),
                reservation.statusDescription()
        );
    }
}
