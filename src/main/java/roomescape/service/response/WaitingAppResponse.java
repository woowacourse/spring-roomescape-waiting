package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.Waiting;

public record WaitingAppResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse) {

    public WaitingAppResponse(Waiting waiting) {
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
                        waiting.getTheme().getThumbnail())
        );
    }
}
