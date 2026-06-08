package roomescape.reservation.application.port.in;

import roomescape.reservation.application.dto.request.ReservationSaveRequest;
import roomescape.reservation.application.dto.response.ReservationSaveResponse;

public interface CreateReservationUseCase {
    ReservationSaveResponse save(ReservationSaveRequest body, long memberId);
}
