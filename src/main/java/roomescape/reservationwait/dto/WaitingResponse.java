package roomescape.reservationwait.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WaitingResponse(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static WaitingResponse from(WaitingResult waitingResponseResult) {
        return new WaitingResponse(
                waitingResponseResult.order(),
                waitingResponseResult.reservationId(),
                waitingResponseResult.memberId(),
                waitingResponseResult.createdAt()
        );
    }

    public static List<WaitingResponse> fromAll(List<WaitingResult> waitingResponseResults) {
        return waitingResponseResults.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
