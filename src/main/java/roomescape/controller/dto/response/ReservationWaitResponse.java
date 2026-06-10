package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Wait;

public sealed interface ReservationWaitResponse permits ReservationResponse, WaitResponse {

    static ReservationWaitResponse from(Reservation reservation) {
        return ReservationResponse.from(reservation);
    }

    static ReservationWaitResponse from(Wait wait, Long order) {
        return WaitResponse.of(wait, order);
    }

    ReservationStatus status();
}
