package roomescape.service.dto.response;

import roomescape.domain.reservation.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(
        long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime time
) {
    public WaitingResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservation().getTheme().getName(),
                waiting.getReservation().getDate(),
                waiting.getReservation().getTime().getStartAt()
        );
    }
}
