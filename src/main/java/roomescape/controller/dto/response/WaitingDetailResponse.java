package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.service.dto.result.WaitingDetailResult;

public record WaitingDetailResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse timeResponse,
        ThemeResponse themeResponse,
        LocalDateTime createdAt,
        int sequence
) {

    public static WaitingDetailResponse from(WaitingDetailResult result) {
        return new WaitingDetailResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.timeResult()),
                ThemeResponse.from(result.themeResult()),
                result.createdAt(),
                result.sequence()
        );
    }
}
