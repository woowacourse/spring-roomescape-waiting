package roomescape.controller.client.api.dto.response;

import java.time.LocalDateTime;
import roomescape.service.result.ReservationEntryResult;

public record ReservationEntryResponse(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static ReservationEntryResponse from(ReservationEntryResult result) {
        if (result == null) {
            return null;
        }
        return new ReservationEntryResponse(
                result.id(),
                result.name(),
                result.status(),
                result.createdAt()
        );
    }
}
