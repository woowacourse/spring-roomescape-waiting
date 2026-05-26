package roomescape.reservationwaiting.dto;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public class ReservationWaitingResponse {

    private final Long id;
    private final String name;
    private final Long reservationId;

    public ReservationWaitingResponse(Long id, String name,  Long reservationId) {
        this.id = id;
        this.name = name;
        this.reservationId = reservationId;
    }

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(), reservationWaiting.getReservation().getId());
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Long reservationId() {
        return reservationId;
    }
}
