package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationV3;

@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationV3, Long> {
}
