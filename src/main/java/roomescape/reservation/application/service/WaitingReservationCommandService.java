package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.WaitingReservation;

public interface WaitingReservationCommandService {

    WaitingReservation create(CreateReservationServiceRequest request);

    void delete(Long id);
}
