package roomescape.service;

import roomescape.repository.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationInfo(LocalDate date, LocalTime time, String theme) {

    public static ReservationInfo from(final Reservation reservation) {
        return new ReservationInfo(reservation.getDate(), reservation.getTime().getStartAt(),
                reservation.getTheme().getName());
    }

    public static ReservationInfo from(final WaitingReservationResponse reservation) {
        return new ReservationInfo(reservation.date(), reservation.time(), reservation.theme());
    }
}
