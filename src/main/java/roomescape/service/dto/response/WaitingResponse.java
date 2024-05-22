package roomescape.service.dto.response;

import roomescape.domain.Waiting;

public record WaitingResponse(
        long id,
        long memberId,
        long reservationId
) {
    public WaitingResponse(Waiting waiting){
        this(
                waiting.getId(),
                waiting.getMember().getId(),
                waiting.getReservation().getId()
        );
    }
}
