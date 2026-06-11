package roomescape.domain.repository;

import java.time.LocalTime;
import java.util.List;

import roomescape.domain.Time;

public interface TimeRepository {
    List<Time> findAll();
    Long save(LocalTime startAt);
    void delete(long id);
}
