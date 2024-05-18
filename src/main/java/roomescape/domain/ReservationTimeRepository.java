package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
