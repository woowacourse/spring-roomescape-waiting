package roomescape.repository;

import roomescape.domain.Time;

import java.util.List;
import java.util.Optional;

public interface TimeRepository {

    List<Time> findAll();

    Optional<Time> findById(long id);

    Time save(Time time);

    void deleteById(long id);
}
