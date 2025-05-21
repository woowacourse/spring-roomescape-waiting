package roomescape.time.application;

import roomescape.time.application.dto.CreateReservationTimeRequest;
import roomescape.time.application.dto.ReservationTimeResponse;
import roomescape.time.domain.ReservationTimeId;

import java.util.List;

public interface ReservationTimeFacade {

    List<ReservationTimeResponse> getAll();

    ReservationTimeResponse create(CreateReservationTimeRequest request);

    void delete(ReservationTimeId id);
}
