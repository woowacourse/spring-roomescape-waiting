package roomescape.domain.waitingreservation.dto;

import roomescape.domain.waitingreservation.WaitingReservation;

public record WaitingReservationWithRank(
    WaitingReservation waitingReservation,
    Long rank
) {
}
