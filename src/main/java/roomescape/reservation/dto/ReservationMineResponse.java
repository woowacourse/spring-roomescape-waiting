package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationMineResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationMineResponse from(final Reservation reservation) {
        return new ReservationMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약");
    }
}
