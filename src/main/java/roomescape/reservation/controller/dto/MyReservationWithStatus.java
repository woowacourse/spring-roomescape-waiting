package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWithStatus(long reservationId, String themeName, LocalDate date, LocalTime time,
                                      ReservationStatus status) {
    public static MyReservationWithStatus from(Reservation memberReservation) {
        return new MyReservationWithStatus(memberReservation.getId(),
                memberReservation.getReservationSlot().getTheme().getName(), memberReservation.getReservationSlot().getDate(),
                memberReservation.getReservationSlot().getTime().getStartAt(), memberReservation.getStatus());
    }
}
