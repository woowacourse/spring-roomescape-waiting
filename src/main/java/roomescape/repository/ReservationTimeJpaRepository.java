package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

@Repository
public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {
}
