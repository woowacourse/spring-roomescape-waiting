package roomescape.dto.response;

import roomescape.domain.Waiting;

import java.time.LocalDateTime;

public record WaitingResponse(
        long id,
        LocalDateTime createdAt,
        long slotId,
        String name,
        ReservationStatus status
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getCreatedAt(),
                waiting.getSlotId(),
                waiting.getName(),
                ReservationStatus.WAITING);
    }
}
