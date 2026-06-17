package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Time;

public interface TimeRepository {

    List<Time> findAll();

    Optional<Time> findById(long id);

    Time save(Time time);

    void deleteById(long id);
}
