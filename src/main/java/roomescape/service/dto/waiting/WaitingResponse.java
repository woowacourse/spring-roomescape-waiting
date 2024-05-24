package roomescape.service.dto.waiting;

import roomescape.domain.reservation.Waiting;

public record WaitingResponse(long id, long reservationId, String createdAt) {

    public WaitingResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getReservation().getId(),
                waiting.getCreatedAt().toString()
        );
    }
}
