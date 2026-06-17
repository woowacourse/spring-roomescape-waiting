package roomescape.time;


import java.time.LocalTime;
import roomescape.common.CommonDao;
import roomescape.time.Time;

public interface TimeDao extends CommonDao<Time> {
    boolean existsByStartAt(LocalTime startAt);
}
