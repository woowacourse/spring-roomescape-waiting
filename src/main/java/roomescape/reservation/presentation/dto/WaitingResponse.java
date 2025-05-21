package roomescape.reservation.presentation.dto;

import roomescape.reservation.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(long id, String name, String theme, LocalDate date, LocalTime startAt) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
            waiting.getId(),
            waiting.getMember().getName(),
            waiting.getReservation().getTheme().getName(),
            waiting.getReservation().getDate(),
            waiting.getReservation().getTime().getStartAt()
        );
    }
}
