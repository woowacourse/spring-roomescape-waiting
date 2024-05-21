package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWithStatus(long reservationId, String themeName, LocalDate date, LocalTime time,
                                      ReservationStatus status) {
    public static MyReservationWithStatus from(MemberReservation memberReservation) {
        return new MyReservationWithStatus(memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(), memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(), memberReservation.getStatus());
    }
}
