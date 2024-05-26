package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.time.domain.Time;

public interface TimeRepository extends JpaRepository<Time, Long> {
    List<Time> findAllByOrderByStartAtAsc();

    boolean existsByStartAt(LocalTime startAt);
}
