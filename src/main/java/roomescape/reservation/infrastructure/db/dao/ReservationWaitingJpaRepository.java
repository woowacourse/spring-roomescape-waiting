package roomescape.reservation.infrastructure.db.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.model.entity.ReservationWaiting;

public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaiting, Long> {

}
