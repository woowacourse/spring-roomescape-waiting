package roomescape.controller.history.dto;

import java.time.LocalDate;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.history.MyHistoryResult;

public record HistoryResponse(
        Long reservationId,
        Long waitingId,
        String status,
        String name,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Integer sequence
) {
    public static HistoryResponse from(final MyHistoryResult history) {
        return new HistoryResponse(
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
