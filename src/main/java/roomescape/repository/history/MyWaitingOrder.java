package roomescape.repository.history;

import java.time.LocalDateTime;

public record MyWaitingOrder(
        Long reservationId,
        Long slotId,
        Long waitingId,
        LocalDateTime requestedAt
) {
}
