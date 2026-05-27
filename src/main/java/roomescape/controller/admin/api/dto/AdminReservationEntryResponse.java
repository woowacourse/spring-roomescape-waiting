package roomescape.controller.admin.api.dto;

import java.time.LocalDateTime;
import roomescape.service.result.ReservationEntryResult;

public record AdminReservationEntryResponse(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static AdminReservationEntryResponse from(ReservationEntryResult result) {
        if (result == null) {
            return null;
        }
        return new AdminReservationEntryResponse(
                result.id(),
                result.name(),
                result.status(),
                result.createdAt()
        );
    }
}
