package roomescape.reservation.waiting;

import roomescape.reservation.reservation.dto.ReservationResponse;

public record WaitingResponse(
        Long id,
        Long rank,
        ReservationResponse reservation
) {

    public static WaitingResponse of(Waiting waiting, ReservationResponse reservationResponse) {
        return new WaitingResponse(waiting.getId(), waiting.getRank(), reservationResponse);
    }
}
