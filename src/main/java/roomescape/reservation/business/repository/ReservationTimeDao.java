package roomescape.reservation.business.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.business.domain.ReservationTime;

public interface ReservationTimeDao {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    int deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existByTime(LocalTime time);
}
