package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.WaitingQueryResult;

public record WaitingResponse(
        Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        WaitingThemeResponse theme, WaitingTimeResponse time, Long order

) {

    public static WaitingResponse from(WaitingQueryResult waitingQueryResult) {
        return new WaitingResponse(waitingQueryResult.id(),
                waitingQueryResult.name(),
                waitingQueryResult.date(),
                new WaitingThemeResponse(waitingQueryResult.themeId(), waitingQueryResult.themeName()),
                new WaitingTimeResponse(waitingQueryResult.timeId(), waitingQueryResult.startAt()),
                waitingQueryResult.order());
    }

    public record WaitingThemeResponse(Long id, String name) {
    }

    public record WaitingTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    }
}
