package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWithStatus(long reservationId, String themeName, LocalDate date, LocalTime time,
                                      ReservationStatus status) {
    public static MyReservationWithStatus from(Reservation reservation) {
        return new MyReservationWithStatus(
                reservation.getId(),
                reservation.getReservationSlot().getTheme().getName(),
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                reservation.getStatus());
    }
}
