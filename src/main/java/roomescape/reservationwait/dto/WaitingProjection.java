package roomescape.reservationwait.dto;

import java.time.LocalDateTime;

public record WaitingProjection(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
}
