package roomescape.controller.dto;

import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationResponse(long id, String name, LocalDate date, TimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getSession().getDate(),
                TimeResponse.from(reservation.getSession().getTimeSlot()),
                ThemeResponse.from(reservation.getSession().getTheme())
        );
    }
}
