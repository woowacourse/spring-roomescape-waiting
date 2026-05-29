package roomescape.feature.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.feature.time.domain.Time;

public interface TimeRepository {

    Time save(Time time);

    List<Time> findAllByDeletedAtIsNull();

    Optional<Time> findTimeByIdAndDeletedAtIsNull(Long id);

    boolean existsTimeByIdAndDeletedAtIsNull(Long id);

    boolean existsTimeByStartAtAndDeletedAtIsNull(LocalTime startAt);

    void deleteTimeById(Long id);
}
