package roomescape.time.service;

import java.util.List;
import roomescape.time.controller.request.AvailableReservationTimeRequest;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeQueryService {

    List<ReservationTimeResponse> getAll();

    ReservationTime getReservationTime(Long id);

    List<AvailableReservationTimeResponse> getAvailableReservationTimes(
            AvailableReservationTimeRequest request);
}
