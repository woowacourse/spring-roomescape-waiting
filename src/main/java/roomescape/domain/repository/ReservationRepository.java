package roomescape.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationResponse;

public interface ReservationRepository {
    List<ReservationResponse> findByUserName(String username);
}
