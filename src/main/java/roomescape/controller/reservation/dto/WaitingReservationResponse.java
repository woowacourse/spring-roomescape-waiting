package roomescape.controller.reservation.dto;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingReservationResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {

    public static WaitingReservationResponse from(final Reservation reservation) {
        return new WaitingReservationResponse(
                reservation.getId(), reservation.getMember().getName(),
                reservation.getTheme().getName(), reservation.getDate(), reservation.getTime().getStartAt()
        );
    }
}
