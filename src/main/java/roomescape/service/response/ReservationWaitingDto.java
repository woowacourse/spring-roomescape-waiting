package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingDto(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeDto reservationTimeDto,
        ThemeDto themeDto,
        String status) {

    public ReservationWaitingDto(ReservationWaiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName().getName(),
                waiting.getDate(),
                new ReservationTimeDto(
                        waiting.getTime().getId(),
                        waiting.getTime().getStartAt()),
                new ThemeDto(waiting.getTheme().getId(),
                        waiting.getTheme().getName(),
                        waiting.getTheme().getDescription(),
                        waiting.getTheme().getThumbnail()),
                waiting.getStatus().name()
        );
    }
}
