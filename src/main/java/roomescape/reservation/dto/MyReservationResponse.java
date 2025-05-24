package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public MyReservationResponse(final Reservation reservation, final String status) {
        this(reservation.getId(), reservation.getTheme().getName().getValue(), reservation.getDate(),
                reservation.getTime().getStartAt(), status);
    }

    public MyReservationResponse(final Waiting waiting, final String status) {
        this(waiting.getId(), waiting.getTheme().getName().getValue(), waiting.getReservationDate(),
                waiting.getReservationTime().getStartAt(), status);
    }
}
