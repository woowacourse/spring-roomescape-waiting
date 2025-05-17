package roomescape.time.application.service;

import roomescape.time.application.dto.CreateReservationTimeServiceRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;

public interface ReservationTimeCommandService {

    ReservationTime create(CreateReservationTimeServiceRequest createReservationTimeServiceRequest);

    void delete(ReservationTimeId id);
}
