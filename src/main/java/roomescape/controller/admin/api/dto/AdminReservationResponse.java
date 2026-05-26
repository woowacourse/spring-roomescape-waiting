package roomescape.controller.admin.api.dto;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record AdminReservationResponse(long id, String name, LocalDate date, AdminReservationTimeResponse time) {

    public static AdminReservationResponse from(ReservationResult result) {
        return new AdminReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                AdminReservationTimeResponse.from(result.time())
        );
    }
}
