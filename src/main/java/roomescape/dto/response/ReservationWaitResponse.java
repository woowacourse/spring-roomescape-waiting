package roomescape.dto.response;

import roomescape.domain.ReservationWait;

import java.time.LocalDateTime;

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
