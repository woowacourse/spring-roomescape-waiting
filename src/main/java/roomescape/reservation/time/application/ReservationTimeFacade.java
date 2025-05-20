package roomescape.reservation.time.application;

import roomescape.reservation.time.application.dto.CreateReservationTimeRequest;
import roomescape.reservation.time.application.dto.ReservationTimeResponse;
import roomescape.reservation.time.domain.ReservationTimeId;

import java.util.List;

public interface ReservationTimeFacade {

    List<ReservationTimeResponse> getAll();

    ReservationTimeResponse create(CreateReservationTimeRequest request);

    void delete(ReservationTimeId id);
}
