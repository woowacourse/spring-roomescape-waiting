package roomescape.time.application.service;

import roomescape.time.application.dto.CreateReservationTimeServiceRequest;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeCommandService {

    ReservationTime create(CreateReservationTimeServiceRequest createReservationTimeServiceRequest);

    void delete(Long id);
}
