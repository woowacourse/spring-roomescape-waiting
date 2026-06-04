package roomescape.service.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.dto.WaitDetailDto;

public sealed interface ServiceReceptionResponse permits ServiceReservationResponse, ServiceWaitResponse {
    static ServiceReceptionResponse from(Reservation reservation) {
        return ServiceReservationResponse.from(reservation);
    }

    static ServiceReceptionResponse from(WaitDetailDto wait) {
        return ServiceWaitResponse.from(wait);
    }

    ReservationStatus status();
}
