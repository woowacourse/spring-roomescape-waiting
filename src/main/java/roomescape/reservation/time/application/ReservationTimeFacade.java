package roomescape.reservation.time.application;

import roomescape.reservation.time.ui.dto.CreateReservationTimeWebRequest;
import roomescape.reservation.time.ui.dto.ReservationTimeResponse;

import java.util.List;

public interface ReservationTimeFacade {

    List<ReservationTimeResponse> getAll();

    ReservationTimeResponse create(CreateReservationTimeWebRequest createReservationTimeWebRequest);

    void delete(Long id);
}
