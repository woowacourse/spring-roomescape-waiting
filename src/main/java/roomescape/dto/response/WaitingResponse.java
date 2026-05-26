package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.service.dto.result.WaitingResult;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse timeResponse,
        ThemeResponse themeResponse,
        LocalDateTime createdAt
) {

    public static WaitingResponse from(WaitingResult result) {
        return new WaitingResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.timeResult()),
                ThemeResponse.from(result.themeResult()),
                result.createdAt()
        );
    }
}
