package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(Long id, String memberName, LocalDate date, LocalTime startAt, String themeName) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName()
        );
    }
}
