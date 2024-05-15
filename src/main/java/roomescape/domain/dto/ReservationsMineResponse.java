package roomescape.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record ReservationsMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time,
                                       String status) {
    public static ReservationsMineResponse from(Reservation reservation) {
        return new ReservationsMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }
}
