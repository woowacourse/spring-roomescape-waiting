package roomescape.reservation.application;

import roomescape.auth.session.UserSession;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.ReservationSearchFilterRequest;
import roomescape.reservation.application.dto.SlotSequenceResponse;
import roomescape.reservation.domain.ReservationId;
import roomescape.user.domain.UserId;

import java.util.List;

public interface ReservationFacade {

    List<ReservationResponse> getAll();

    List<ReservationResponse> getAllBySearchFilter(ReservationSearchFilterRequest request);

    List<SlotSequenceResponse> getAllSlotSequenceByUserId(UserId userId);

    ReservationResponse create(CreateReservationRequest request);

    void delete(ReservationId id, UserSession userSession);
}
