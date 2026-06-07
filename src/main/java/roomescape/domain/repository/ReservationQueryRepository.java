package roomescape.domain.repository;

import java.util.List;

import roomescape.domain.Reservation;
import roomescape.dto.ReservationResponse;

public interface ReservationQueryRepository {
    Reservation findById(Long id);
    List<ReservationResponse> findByUserName(String username);
}
