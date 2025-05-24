package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt
) {
    public static WaitingResponse from(final Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt()
        );
    }

}

