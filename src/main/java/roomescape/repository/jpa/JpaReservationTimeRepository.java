package roomescape.repository.jpa;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAllByOrderByStartAtAsc();

    boolean existsByStartAt(LocalTime startAt);
}
