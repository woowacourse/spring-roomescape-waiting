package roomescape.reservation.application;

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

    List<ReservationResponse> getAllByUserId(Long userId);

    ReservationResponse create(CreateReservationWithUserIdWebRequest request);

    void delete(Long id);
}
