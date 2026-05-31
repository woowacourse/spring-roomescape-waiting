package roomescape.repository.dto;

import roomescape.domain.ReservationWaiting;

public record WaitingWithTurn(
        ReservationWaiting waiting,
        Long turn
) {
}
