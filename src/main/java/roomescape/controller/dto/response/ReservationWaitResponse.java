package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.WaitInfo;

public sealed interface ReservationWaitResponse permits ReservationResponse, WaitResponse {

    static ReservationWaitResponse from(Reservation reservation) {
        return ReservationResponse.from(reservation);
    }

    static ReservationWaitResponse from(WaitInfo waitInfo) {
        return WaitResponse.from(waitInfo);
    }

    ReservationStatus status();
}
