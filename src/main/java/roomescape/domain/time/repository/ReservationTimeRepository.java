package roomescape.domain.time.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.time.domain.ReservationTime;

import java.time.LocalTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
