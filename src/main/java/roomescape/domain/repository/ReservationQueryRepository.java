package roomescape.domain.repository;

import java.util.List;

import roomescape.dto.ReservationResponse;

public interface ReservationQueryRepository {
    List<ReservationResponse> findByUserName(String username);
}
