package roomescape.controller.client.api.dto.response;

import java.time.LocalDateTime;
import roomescape.service.result.ReservationResult;

public record ReservationResponse(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static ReservationResponse from(ReservationResult result) {
        if (result == null) {
            return null;
        }
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.status(),
                result.createdAt()
        );
    }
}
