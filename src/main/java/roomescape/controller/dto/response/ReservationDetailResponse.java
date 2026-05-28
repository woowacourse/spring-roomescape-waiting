package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.service.dto.result.ReservationDetailResult;

public record ReservationDetailResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse timeResponse,
        ThemeResponse themeResponse,
        ReservationStatus status,
        Integer sequence
) {

    public static ReservationDetailResponse from(ReservationDetailResult result) {
        return new ReservationDetailResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.timeResult()),
                ThemeResponse.from(result.themeResult()),
                result.status(),
                result.sequence()
        );
    }
}