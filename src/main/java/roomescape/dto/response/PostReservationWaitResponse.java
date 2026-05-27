package roomescape.dto.response;

import java.time.LocalDateTime;
import roomescape.domain.ReservationWait;

public record PostReservationWaitResponse(
        Long id,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static PostReservationWaitResponse from(ReservationWait wait) {
        return new PostReservationWaitResponse(
                wait.getId(),
                wait.getReservationId(),
                wait.getMemberId(),
                wait.getCreatedAt()
        );
    }
}
