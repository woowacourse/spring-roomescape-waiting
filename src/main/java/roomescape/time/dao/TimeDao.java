package roomescape.time.dao;

import java.time.LocalTime;
import java.util.List;
import roomescape.time.domain.Time;

public interface TimeDao {

    Time save(Time reservationTime);

    List<Time> findAllReservationTimesInOrder();

    Time findById(long reservationTimeId);

    void deleteById(long reservationTimeId);

    int countByStartAt(LocalTime startAt);

}
