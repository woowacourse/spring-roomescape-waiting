package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservation(
        Long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status) {

    public MemberReservation(Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getValue()
        );
    }
}
