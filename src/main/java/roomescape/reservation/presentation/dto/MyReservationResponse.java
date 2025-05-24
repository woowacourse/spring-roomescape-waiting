package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            "예약"
        );
    }
}
