package roomescape.dto.response;

import java.util.List;
import roomescape.domain.ReservationWait;

import java.time.LocalDateTime;
import roomescape.dto.WaitingResponseResult;

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

    public static List<ReservationWaitResponse> fromAll(List<WaitingResponseResult> waitingResponseResults) {
        return waitingResponseResults.stream()
                .map(waitingResponseResults::from)
                .toList();
    }
}
