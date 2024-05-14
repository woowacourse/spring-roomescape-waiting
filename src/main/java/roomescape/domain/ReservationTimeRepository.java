package roomescape.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    int deleteById(Long id);

    List<ReservationTime> findAll();

    boolean isStartTimeExists(LocalTime startTime);
}
