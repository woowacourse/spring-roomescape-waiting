package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.Reservation;

public interface ReservationCommandService {

    Reservation create(CreateReservationServiceRequest createReservationServiceRequest);

    void delete(Long id);

    void updateUserId(Long id, Long userId);
}
