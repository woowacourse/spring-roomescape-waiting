package roomescape.reservation.dao.reservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import roomescape.reservation.model.ReservationTime;

public interface ReservationTimeDao {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    int deleteById(Long id);

    boolean existsByStartAt(LocalTime startAt);
}
