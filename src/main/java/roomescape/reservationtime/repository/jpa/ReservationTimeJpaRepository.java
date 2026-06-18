package roomescape.reservationtime.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.repository.entity.ReservationTimeEntity;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTimeEntity, Long> {
}
