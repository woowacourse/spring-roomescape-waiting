package roomescape.dto;

import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(ReservationWithRank reservation) {
        ReservationStatus reservationStatus = reservation.getStatus();
        String statusMessage = reservationStatus.makeStatusMessage(reservation.getRank());

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime(),
                statusMessage
        );
    }
}
