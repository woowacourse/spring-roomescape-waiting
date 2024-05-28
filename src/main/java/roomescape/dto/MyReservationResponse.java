package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.dto.service.ReservationWithRank;

public record MyReservationResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(ReservationWithRank reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getStatusMessage()
        );
    }
}
