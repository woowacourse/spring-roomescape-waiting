package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTimeV2;

@Repository
public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTimeV2, Long> {
}
