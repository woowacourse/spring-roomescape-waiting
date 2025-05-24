package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public MyReservationResponse(final Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                convertToReservationStatusMessage(reservation)
        );
    }

    private static String convertToReservationStatusMessage(final Reservation reservation) {
        StringBuilder status = new StringBuilder();
        if (reservation.getReservationStatus().getStatus() == Status.WAITING) {
            status.append(reservation.getReservationStatus().getRank());
        }
        status.append(reservation.getReservationStatus().getStatus().getOutput());
        return status.toString();
    }
}
