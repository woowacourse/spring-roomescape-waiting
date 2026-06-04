package roomescape.reservationwait.dto;

import java.time.LocalDateTime;

public record WaitingResult(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static WaitingResult from(WaitingProjection projection) {
        return new WaitingResult(
                projection.order(),
                projection.reservationId(),
                projection.memberId(),
                projection.createdAt()
        );
    }
}
