package roomescape.dto;

import java.time.LocalDateTime;

public record WaitingResponseResult(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static WaitingResponseResult from(WaitingResponseProjection projection) {
        return new WaitingResponseResult(
                projection.order(),
                projection.reservationId(),
                projection.memberId(),
                projection.createdAt()
        );
    }
}
