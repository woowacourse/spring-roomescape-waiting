package roomescape.dto.response;

import java.time.LocalDateTime;
import roomescape.domain.Waiting;

public record WaitingWithRankResponse(
        long id,
        LocalDateTime createdAt,
        long slotId,
        String name,
        int rank
) {
    public static WaitingWithRankResponse from(Waiting waiting, int rank) {
        return new WaitingWithRankResponse(waiting.getId(), waiting.getCreatedAt(), waiting.getSlotId(), waiting.getName(), rank);
    }
}
