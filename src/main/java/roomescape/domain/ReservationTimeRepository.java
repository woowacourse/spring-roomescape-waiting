package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.Optional;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

    ReservationTime findReservationTimeById(Long timeId);
}
