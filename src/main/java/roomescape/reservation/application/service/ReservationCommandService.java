package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;

public interface ReservationCommandService {

    Reservation create(CreateReservationServiceRequest createReservationServiceRequest);

    void delete(ReservationId id);
}
