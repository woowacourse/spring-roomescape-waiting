package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.time.domain.Time;

public interface TimeRepository extends Repository<Time, Long> {

    Time save(Time time);

    List<Time> findAllByOrderByStartAt();

    Optional<Time> findByStartAt(LocalTime startAt);

    Optional<Time> findById(long id);

    void deleteById(long timeId);
}
