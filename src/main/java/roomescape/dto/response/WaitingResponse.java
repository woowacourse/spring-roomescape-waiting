package roomescape.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import roomescape.dto.WaitingResponseResult;

public record WaitingResponse(
        Long order,
        Long reservationId,
        Long memberId,
        LocalDateTime createdAt
) {
    public static WaitingResponse from(WaitingResponseResult waitingResponseResult) {
        return new WaitingResponse(
                waitingResponseResult.order(),
                waitingResponseResult.reservationId(),
                waitingResponseResult.memberId(),
                waitingResponseResult.createdAt()
        );
    }

    public static List<WaitingResponse> fromAll(List<WaitingResponseResult> waitingResponseResults) {
        return waitingResponseResults.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
