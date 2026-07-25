package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.Session;

public record SessionResponse(Long id, LocalDate date, TimeResponse time, ThemeResponse theme) {

    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getDate(),
                TimeResponse.from(session.getTimeSlot()),
                ThemeResponse.from(session.getTheme())
        );
    }
}
