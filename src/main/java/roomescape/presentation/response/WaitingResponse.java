package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        long id,
        UserResponse user,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme
) {

    public static List<WaitingResponse> fromWaitings(
            final List<Waiting> waitings
    ) {
        return waitings.stream()
                .map(WaitingResponse::fromWaiting)
                .toList();
    }

    public static WaitingResponse fromWaiting(
            final Waiting waiting
    ) {
        return new WaitingResponse(
                waiting.id(),
                UserResponse.fromUser(waiting.user()),
                waiting.date(),
                TimeSlotResponse.fromTimeSlot(waiting.timeSlot()),
                ThemeResponse.fromTheme(waiting.theme())
        );
    }
}
