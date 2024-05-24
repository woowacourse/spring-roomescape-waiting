package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingAppResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse,
        String status) {

    public ReservationWaitingAppResponse(ReservationWaiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName().getName(),
                waiting.getDate(),
                new ReservationTimeAppResponse(
                        waiting.getTime().getId(),
                        waiting.getTime().getStartAt()),
                new ThemeAppResponse(waiting.getTheme().getId(),
                        waiting.getTheme().getName(),
                        waiting.getTheme().getDescription(),
                        waiting.getTheme().getThumbnail()),
                waiting.getStatus().name()
        );
    }
}
