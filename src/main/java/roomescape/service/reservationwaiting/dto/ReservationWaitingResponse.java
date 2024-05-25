package roomescape.service.reservationwaiting.dto;

import roomescape.domain.reservationwaiting.ReservationWaiting;

public class ReservationWaitingResponse {
    private final Long waitingId;
    private final Long reservationId;

    public ReservationWaitingResponse(Long waitingId, Long reservationId) {
        this.waitingId = waitingId;
        this.reservationId = reservationId;
    }

    public ReservationWaitingResponse(ReservationWaiting waiting) {
        this(waiting.getId(), waiting.getReservation().getId());
    }

    public Long getWaitingId() {
        return waitingId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
