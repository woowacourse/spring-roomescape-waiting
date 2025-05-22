package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

}
