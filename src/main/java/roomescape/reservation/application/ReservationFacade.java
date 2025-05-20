package roomescape.reservation.application;

import roomescape.auth.session.UserSession;
import roomescape.reservation.ui.ReservationSearchWebRequest;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReservationFacade {

    List<ReservationResponse> getAll();

    List<AvailableReservationTimeWebResponse> getAvailable(LocalDate date, Long themeId);

    List<ReservationResponse> getByParams(ReservationSearchWebRequest request);

    List<ReservationResponse> getAllByUserId(Long userId);

    ReservationResponse create(CreateReservationWithUserIdWebRequest request, UserSession userSession);

    void delete(Long id);
}
