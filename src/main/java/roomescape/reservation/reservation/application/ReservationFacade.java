package roomescape.reservation.reservation.application;

import roomescape.auth.session.UserSession;
import roomescape.reservation.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.reservation.application.dto.ReservationSearchRequest;
import roomescape.reservation.reservation.domain.ReservationId;
import roomescape.reservation.reservation.application.dto.ReservationResponse;
import roomescape.user.domain.UserId;

import java.util.List;

public interface ReservationFacade {

    List<ReservationResponse> getAll();

    List<ReservationResponse> getByParams(ReservationSearchRequest request);

    List<ReservationResponse> getAllByUserId(UserId id);

    ReservationResponse create(CreateReservationRequest request, UserSession userSession);

    void delete(ReservationId id);
}
