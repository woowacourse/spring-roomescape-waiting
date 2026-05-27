package roomescape.service.dto.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.dao.dto.WaitingQueryResult;

public record WaitingDetailResult (
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult timeResult,
        ThemeResult themeResult,
        LocalDateTime createdAt,
        int sequence
) {

    public static WaitingDetailResult from(WaitingQueryResult queryResult) {
        return new WaitingDetailResult(
                queryResult.id(),
                queryResult.name().value(),
                queryResult.date(),
                ReservationTimeResult.from(queryResult.time()),
                ThemeResult.from(queryResult.theme()),
                queryResult.createdAt(),
                queryResult.sequence()
        );
    }
}
