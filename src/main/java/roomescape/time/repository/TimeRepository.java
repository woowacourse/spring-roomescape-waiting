package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import roomescape.time.domain.Time;

public interface TimeRepository extends JpaRepository<Time, Long> {
    List<Time> findAllByOrderByStartAtAsc();

    int countByStartAt(LocalTime startAt);
}
