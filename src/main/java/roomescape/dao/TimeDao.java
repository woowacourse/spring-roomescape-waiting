package roomescape.dao;


import java.time.LocalTime;
import roomescape.domain.Time;

public interface TimeDao extends CommonDao<Time> {
    boolean existsByStartAt(LocalTime startAt);
}
