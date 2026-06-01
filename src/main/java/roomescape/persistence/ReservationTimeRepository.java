package roomescape.persistence;

import java.time.LocalTime;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(long id);

    boolean existsByStartAt(LocalTime time);

    void update(ReservationTime time);
}
