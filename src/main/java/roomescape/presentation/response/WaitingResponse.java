package roomescape.presentation.response;

import java.time.LocalDate;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        long id,
        UserResponse user,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.id(),
                UserResponse.from(waiting.user()),
                waiting.date(),
                TimeSlotResponse.from(waiting.timeSlot()),
                ThemeResponse.from(waiting.theme())
        );
    }
}
