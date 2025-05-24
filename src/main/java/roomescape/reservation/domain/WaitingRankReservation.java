package roomescape.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingRankReservation {

    private final Reservation reservation;
    private final Long waitingRank;
}
