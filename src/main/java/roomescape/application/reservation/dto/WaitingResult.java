package roomescape.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.ThemeSchedule;
import roomescape.domain.reservation.Waiting;

public record WaitingResult(
        long waitingId,
        String memberName,
        String themeName,
        LocalDate date,
        LocalDateTime startedAt
) {
    public static WaitingResult from(Waiting waiting) {
        ThemeSchedule themeSchedule = waiting.getThemeSchedule();
        return new WaitingResult(
                waiting.getId(),
                waiting.getMember().getName(),
                themeSchedule.theme().getName(),
                themeSchedule.date(),
                waiting.getStartedAt()
        );
    }
}
