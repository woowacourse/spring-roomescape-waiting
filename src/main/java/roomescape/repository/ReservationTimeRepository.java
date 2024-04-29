package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    ReservationTime findById(Long id);

    int deleteById(Long id);

    List<ReservationTime> findAll();

    Boolean isStartTimeExists(LocalTime startTime);
}
