package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationItem;

@Repository
public interface ReservationItemJpaRepository extends JpaRepository<ReservationItem, Long> {
}
