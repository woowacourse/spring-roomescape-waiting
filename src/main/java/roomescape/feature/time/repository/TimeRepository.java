package roomescape.feature.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.feature.time.domain.Time;

public interface TimeRepository {

    Time save(Time time);

    List<Time> findAllByNotDeleted();

    Optional<Time> findTimeByIdAndNotDeleted(Long id);

    boolean existsTimeByIdAndNotDeleted(Long id);

    boolean existsTimeByStartAtAndNotDeleted(LocalTime startAt);

    void deleteTimeById(Long id);
}
