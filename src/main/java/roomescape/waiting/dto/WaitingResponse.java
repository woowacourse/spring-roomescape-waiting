package roomescape.waiting.dto;

import roomescape.reservation.dto.ReservationResponse;
import roomescape.waiting.Waiting;

public record WaitingResponse(
        Long id,
        Long rank,
        ReservationResponse reservationResponse
) {

    public static WaitingResponse of(Waiting waiting, ReservationResponse reservationResponse) {
        return new WaitingResponse(waiting.getId(), waiting.getRank(), reservationResponse);
    }
}
