package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

@Repository
public interface ReservationTimeJpaRepository extends ReservationTimeRepository, JpaRepository<ReservationTime, Long> {

}
