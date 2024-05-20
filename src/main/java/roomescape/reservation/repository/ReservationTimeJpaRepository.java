package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime localTime);
}
