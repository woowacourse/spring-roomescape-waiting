package roomescape.controller.dto.response;

import java.util.List;

public record ReservationWaitingsResponse(List<ReservationWaitingResponse> reservations) {
}
