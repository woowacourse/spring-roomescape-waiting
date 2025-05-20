package roomescape.reservation.application;

import roomescape.auth.session.UserSession;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.application.dto.ReservationSearchRequest;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.user.domain.UserId;

import java.util.List;

public interface ReservationFacade {

    List<ReservationResponse> getAll();

    List<ReservationResponse> getByParams(ReservationSearchRequest request);

    List<ReservationResponse> getAllByUserId(UserId id);

    ReservationResponse create(CreateReservationRequest request, UserSession userSession);

    void delete(ReservationId id);
}
