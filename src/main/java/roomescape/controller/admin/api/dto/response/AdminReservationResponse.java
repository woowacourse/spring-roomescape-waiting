package roomescape.controller.admin.api.dto.response;

import java.time.LocalDateTime;
import roomescape.service.result.ReservationResult;

public record AdminReservationResponse(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static AdminReservationResponse from(ReservationResult result) {
        if (result == null) {
            return null;
        }
        return new AdminReservationResponse(
                result.id(),
                result.name(),
                result.status(),
                result.createdAt()
        );
    }
}
