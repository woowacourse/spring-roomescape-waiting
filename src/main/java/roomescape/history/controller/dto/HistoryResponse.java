package roomescape.history.controller.dto;

import java.time.LocalDate;
import roomescape.history.ReservationHistoryStatus;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record HistoryResponse(
        Long reservationId,
        Long waitingId,
        ReservationHistoryStatus status,
        String name,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Integer sequence
) {
}
