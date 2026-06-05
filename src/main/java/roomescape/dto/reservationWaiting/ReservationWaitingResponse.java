package roomescape.dto.reservationWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationWaitingResponse(Long id, String name, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, Long sequence, LocalDateTime createdAt) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservationWaiting.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservationWaiting.getTheme());
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(), reservationWaiting.getDate(), reservationTimeResponse, themeResponse,
                reservationWaiting.getSequence(), reservationWaiting.getCreatedAt());
    }
}
