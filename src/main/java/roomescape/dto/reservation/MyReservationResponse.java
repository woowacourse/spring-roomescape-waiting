package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        String time,
        String status
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt().format(FORMATTER),
                reservation.getStatus().getValue()
        );
    }
}
