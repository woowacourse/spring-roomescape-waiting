package roomescape.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.reservation.domain.Reservation;

@Getter
@AllArgsConstructor
public class WaitingRankReservation {

    private final Reservation reservation;
    private final Long waitingRank;
}
