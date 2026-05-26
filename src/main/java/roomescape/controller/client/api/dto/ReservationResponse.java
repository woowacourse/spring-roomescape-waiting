package roomescape.controller.client.api.dto;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record ReservationResponse(long id, String name, LocalDate date, ReservationTimeResponse time) {

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time())
        );
    }
}
