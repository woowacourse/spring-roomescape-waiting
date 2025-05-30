package roomescape.reservation.application;

import roomescape.reservation.application.dto.SimpleWaitingReservationResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.WaitingReservationResponse;

import java.util.List;

public interface WaitingReservationFacade {

    List<WaitingReservationResponse> getAll();

    SimpleWaitingReservationResponse create(CreateReservationWithUserIdWebRequest request);

    void delete(Long id);

    ReservationResponse promotion(Long id, CreateReservationWithUserIdWebRequest requestWithUserId);
}
