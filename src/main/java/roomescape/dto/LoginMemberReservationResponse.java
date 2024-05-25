package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record LoginMemberReservationResponse(
        long id,
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
