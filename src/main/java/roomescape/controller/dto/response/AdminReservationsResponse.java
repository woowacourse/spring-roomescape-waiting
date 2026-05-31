package roomescape.controller.dto.response;

import java.util.List;

public record AdminReservationsResponse(
        List<AdminReservationResponse> reservations,
        long totalCount,
        int page,
        int size
) {
}
