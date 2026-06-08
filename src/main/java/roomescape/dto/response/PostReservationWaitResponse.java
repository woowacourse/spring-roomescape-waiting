package roomescape.dto.response;

import roomescape.domain.ReservationWait;
import roomescape.dto.result.CreatedWaitResult;

import java.time.LocalDateTime;

public record PostReservationWaitResponse(
        Long id,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt,
        Long order
) {
    public static PostReservationWaitResponse from(CreatedWaitResult result) {
        ReservationWait wait = result.reservationWait();
        return new PostReservationWaitResponse(
                wait.getId(),
                wait.getReservationId(),
                wait.getMemberId(),
                wait.getCreatedAt(),
                result.order()
        );
    }
}
