package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        ThemeResponse theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MemberReservationResponse from(Reservation reservation) {
        return  new MemberReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                "예약");
    }
}
