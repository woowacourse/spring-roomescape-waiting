package roomescape.reservation.model.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.reservation.model.entity.ReservationWaiting;

@AllArgsConstructor
@Getter
public final class ReservationWaitingWithRank {

    private final ReservationWaiting reservationWaiting;
    private final Long rank;

    public int getRankToInt() {
        return rank.intValue();
    }
}
