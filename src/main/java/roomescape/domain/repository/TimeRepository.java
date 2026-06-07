package roomescape.domain.repository;

import java.time.LocalTime;
import java.util.List;

import roomescape.domain.Time;

public interface TimeRepository {
    Long save(LocalTime startAt);
    Time findById(long id);
    List<Time> findAll();
    void delete(Long id);
}
