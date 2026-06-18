package roomescape.time.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findReservationTimeByStartAt(LocalDateTime startAt);
}
