package roomescape.reservation.application;

import roomescape.reservation.application.dto.MyReservationsResponse;
import roomescape.reservation.application.dto.WaitingReservationResponse;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;

import java.time.LocalDate;
import java.util.List;

public interface ReservationFacade {

    List<ReservationResponse> getAll();

    List<AvailableReservationTimeWebResponse> getAvailable(LocalDate date, Long themeId);

    List<ReservationResponse> getByParams(ReservationSearchWebRequest request);

    List<MyReservationsResponse> getAllByUserId(Long userId);

    ReservationResponse create(CreateReservationWithUserIdWebRequest request);

    void delete(Long id);

    WaitingReservationResponse addWaiting(CreateReservationWithUserIdWebRequest request);

    void deleteWaiting(Long id);
}
