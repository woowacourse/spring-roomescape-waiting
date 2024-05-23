package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        String status,
        Long rank
) {

    public static MyReservationResponse of(Reservation reservation, Long rank) {
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getTime().getStartAt(), reservation.getStatus().getStatus(), rank);
    }
}
