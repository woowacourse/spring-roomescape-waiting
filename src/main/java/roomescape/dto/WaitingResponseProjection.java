package roomescape.dto;

import java.time.LocalDateTime;

public record WaitingResponseProjection(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
}
