package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationsResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status,
        Long rank
) {

    public static MyReservationsResponse from(Reservation reservation) {
        return new MyReservationsResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                ReservationStatus.CONFIRMED.getDescription(),
                null
        );
    }
}
