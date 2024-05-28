package roomescape.domain.dto;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWaitingResponse(Long id, String name, String theme, LocalDate date, LocalTime startAt) {
    public static ReservationWaitingResponse from(Reservation reservation) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
