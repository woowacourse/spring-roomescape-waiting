package roomescape.dto.reservationWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationWaitingResponse(Long id, String name, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, Long sequence, LocalDateTime createdAt) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservationWaiting.getReservation().getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservationWaiting.getReservation().getTheme());
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(), reservationWaiting.getReservation().getDate(), reservationTimeResponse, themeResponse,
                reservationWaiting.getSequence(), reservationWaiting.getCreatedAt());
    }
}
