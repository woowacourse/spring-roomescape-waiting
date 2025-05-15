package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public MyReservationResponse(final Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName().getValue(), reservation.getDate(),
                reservation.getTime().getStartAt(), "예약");
    }
}
