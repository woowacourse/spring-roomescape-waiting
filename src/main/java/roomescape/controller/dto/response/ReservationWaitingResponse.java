package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        LocalTime time,
        long waitingNumber
) {
    public static ReservationWaitingResponse from(ReservationWaiting waiting) {
        return new ReservationWaitingResponse(
                waiting.id(),
                waiting.reservation().getDate(),
                waiting.reservation().getTheme().getName(),
                waiting.reservation().getTheme().getDescription(),
                waiting.reservation().getTheme().getThumbnailUrl(),
                waiting.reservation().getTime().getStartAt(),
                waiting.waitingNumber()
        );
    }
}
