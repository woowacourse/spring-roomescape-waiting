package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record LoginMemberReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        ReservationStatus status
) {
    public static LoginMemberReservationResponse from(Reservation reservation) {
        return new LoginMemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getStatus()
        );
    }
}
