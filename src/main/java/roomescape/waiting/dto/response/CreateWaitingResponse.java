package roomescape.waiting.dto.response;

import roomescape.waiting.model.Waiting;

public record CreateWaitingResponse(Long waitingId,
                                    Long memberId,
                                    Long reservationId) {
    public static CreateWaitingResponse of(final Waiting waiting) {
        return new CreateWaitingResponse(
                waiting.getId(),
                waiting.getMember().getId(),
                waiting.getReservation().getId());
    }
}
