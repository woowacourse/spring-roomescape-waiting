package roomescape.controller.dto;

import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationResponse(long id, String name, LocalDate date, TimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTimeSlot()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
