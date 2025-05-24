package roomescape.reservation.domain.waiting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@NoArgsConstructor
@Accessors(fluent = true)
public class ReservationWaitingWithRank {

    private ReservationWaiting reservationWaiting;
    private Long rank;

    public ReservationWaitingWithRank(final ReservationWaiting reservationWaiting, final Long rank) {
        this.reservationWaiting = reservationWaiting;
        this.rank = rank;
    }
}
