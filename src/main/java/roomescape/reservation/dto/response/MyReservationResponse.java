package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        ReservationStatus status
) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getTime().getStartAt(), reservation.getStatus());
    }
}
