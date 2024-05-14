package roomescape.time.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.Time;

public interface TimeDao {

    Time save(Time reservationTime);

    List<Time> findAllReservationTimesInOrder();

    Optional<Time> findById(long reservationTimeId);

    Optional<Time> findByStartAt(LocalTime startAt);

    void deleteById(long reservationTimeId);
}
