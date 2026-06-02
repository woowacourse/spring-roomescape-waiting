package roomescape.controller.history.dto;

import java.time.LocalDate;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.history.MyReservationHistory;
import roomescape.service.history.ReservationHistoryStatus;

public record MyReservationHistoryResponse(
        Long reservationId,
        Long waitingId,
        ReservationHistoryStatus status,
        String name,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Integer sequence
) {

    public static MyReservationHistoryResponse from(final MyReservationHistory history) {
        return new MyReservationHistoryResponse(
                history.reservationId(),
                history.waitingId(),
                history.status(),
                history.name(),
                history.date(),
                ThemeResponse.from(history.theme()),
                ReservationTimeResponse.from(history.time()),
                history.sequence()
        );
    }
}
