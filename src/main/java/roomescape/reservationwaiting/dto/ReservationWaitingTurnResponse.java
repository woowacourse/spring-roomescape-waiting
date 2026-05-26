package roomescape.reservationwaiting.dto;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public class ReservationWaitingTurnResponse {

    private final Long id;
    private final String name;
    private final Long reservationId;
    private final Long turn;

    public ReservationWaitingTurnResponse(Long id, String name, Long reservationId, Long turn) {
        this.id = id;
        this.name = name;
        this.reservationId = reservationId;
        this.turn = turn;
    }

    public static ReservationWaitingTurnResponse from(ReservationWaiting reservationWaiting, Long turn) {
        return new ReservationWaitingTurnResponse(reservationWaiting.getId(), reservationWaiting.getName(),
                reservationWaiting.getReservation().getId(), turn);
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

    public Long turn() {
        return turn;
    }
}
