package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingResponse(Long waitingId, String name, String themeName, LocalDate date, LocalTime time) {

    public static WaitingResponse from(final Reservation reservation) {
        return new WaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
