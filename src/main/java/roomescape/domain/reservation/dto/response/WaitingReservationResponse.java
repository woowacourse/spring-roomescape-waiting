package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.domain.reservation.Reservation;

public record WaitingReservationResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {

    public static WaitingReservationResponse from(Reservation reservation) {
        return new WaitingReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
