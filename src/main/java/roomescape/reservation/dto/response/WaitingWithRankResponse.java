package roomescape.reservation.dto.response;

import roomescape.reservation.domain.WaitingWithRank;

public record WaitingWithRankResponse(Long rank, ReservationResponse reservation) {
    public static WaitingWithRankResponse from(WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResponse(
                waitingWithRank.getRank(),
                ReservationResponse.from(waitingWithRank.getWaiting())
        );
    }
}
