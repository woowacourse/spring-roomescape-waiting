package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;

import java.util.Optional;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime fetchById(long id) {
        return findById(id).orElseThrow();
    }
}
