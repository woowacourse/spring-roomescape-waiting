package roomescape.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);
}
