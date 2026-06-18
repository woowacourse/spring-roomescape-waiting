package roomescape.repository.reservationwaiting.jpa;

import java.time.LocalDateTime;

public record WaitingWithRank(
        Long waitingId,
        Long slotId,
        String name,
        LocalDateTime requestedAt,
        Long rank
) {
}
