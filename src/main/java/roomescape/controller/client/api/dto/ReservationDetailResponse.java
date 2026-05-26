package roomescape.controller.client.api.dto;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record ReservationDetailResponse(
        long id, String name, LocalDate date, long themeId, ReservationTimeResponse time) {

    public static ReservationDetailResponse from(ReservationResult result) {
        return new ReservationDetailResponse(
                result.id(),
                result.name(),
                result.date(),
                result.theme().id(),
                ReservationTimeResponse.from(result.time())
        );
    }
}
