package roomescape.reservation.dto;

import roomescape.reservation.domain.MemberReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(Long id, String memberName, LocalDate date, LocalTime startAt, String themeName) {

    public static ReservationResponse from(MemberReservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getReservation().getDate(),
                reservation.getReservation().getTime().getStartAt(),
                reservation.getReservation().getTheme().getName()
        );
    }
}
