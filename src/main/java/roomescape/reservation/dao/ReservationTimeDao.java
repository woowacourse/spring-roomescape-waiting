package roomescape.reservation.dao;

import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.model.ReservationTime;

public interface ReservationTimeDao {

    ReservationTime add(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    ReservationTime findById(Long id);

    int deleteById(Long id);

    boolean existByStartAt(LocalTime startAt);
}
