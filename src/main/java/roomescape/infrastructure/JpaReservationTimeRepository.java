package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

}
