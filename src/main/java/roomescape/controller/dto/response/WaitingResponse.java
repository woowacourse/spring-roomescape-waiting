package roomescape.controller.dto.response;

import roomescape.domain.Schedule;
import roomescape.domain.Waiting;
import roomescape.service.dto.WaitingResult;

import java.time.LocalDate;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long order
) {

    public static WaitingResponse from(WaitingResult waitingResult) {
        Waiting waiting = waitingResult.waiting();
        Schedule schedule = waiting.getSchedule();
        return new WaitingResponse(
                waiting.getId(),
                waiting.getName(),
                schedule.getDate(),
                ReservationTimeResponse.from(schedule.getTime()),
                ThemeResponse.from(schedule.getTheme()),
                waitingResult.order()
                );
    }
}
