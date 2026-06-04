package roomescape.reservationwait.dto;

import java.time.LocalDateTime;
import roomescape.reservationwait.ReservationWait;

public record ReservationWaitResponse(
        Long id,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static ReservationWaitResponse from(ReservationWait wait) {
        return new ReservationWaitResponse(
                wait.getId(),
                wait.getReservationId(),
                wait.getMemberId(),
                wait.getCreatedAt()
        );
    }
}
