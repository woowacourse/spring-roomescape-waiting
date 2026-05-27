package roomescape.dto.response;

import roomescape.domain.ReservationWait;

import java.time.LocalDateTime;

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
