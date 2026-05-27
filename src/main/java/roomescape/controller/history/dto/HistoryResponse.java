package roomescape.controller.history.dto;

import java.time.LocalDate;
import roomescape.controller.history.ReservationHistoryStatus;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;

public record HistoryResponse(
        ReservationHistoryStatus status,
        String name,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Integer sequence
) {
}
