package roomescape.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ThemeSchedule;
import roomescape.domain.reservation.WaitingRank;

public record WaitingResult(
        Long waitingId,
        String theme,
        LocalDate date,
        LocalTime time,
        long rank
) {
    public static WaitingResult from(WaitingRank waitingRank) {
        ThemeSchedule themeSchedule = waitingRank.waiting().getThemeSchedule();
        return new WaitingResult(
                waitingRank.waiting().getId(),
                themeSchedule.theme().getName(),
                themeSchedule.date(),
                themeSchedule.time().getStartAt(),
                waitingRank.rank()
        );
    }
}
