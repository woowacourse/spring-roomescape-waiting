package roomescape.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Waiting;
import roomescape.service.dto.WaitingWithNumber;

public record WaitingResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        LocalDateTime createdAt,
        int waitingNumber
) {

    public static WaitingResponse from(WaitingWithNumber waitingWithNumber) {
        Waiting waiting = waitingWithNumber.waiting();
        return new WaitingResponse(waiting.getId(), waiting.getName(), waiting.getDate(),
                TimeResponse.from(waiting.getTimeSlot()),
                ThemeResponse.from(waiting.getTheme()), waiting.getCreatedAt(), waitingWithNumber.waitingNumber());
    }
}

