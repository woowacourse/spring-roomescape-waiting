package roomescape.reservationtime.application.port.in;

import roomescape.reservationtime.application.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.application.dto.response.ReservationTimeSaveResponse;

public interface CreateReservationTimeUseCase {
    ReservationTimeSaveResponse save(ReservationTimeSaveRequest body);
}
