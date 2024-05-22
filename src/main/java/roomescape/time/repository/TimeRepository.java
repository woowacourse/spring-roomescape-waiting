package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import roomescape.time.domain.Time;

public interface TimeRepository extends CrudRepository<Time, Long> {
    List<Time> findAllByOrderByStartAtAsc();

    int countByStartAt(LocalTime startAt);
}
