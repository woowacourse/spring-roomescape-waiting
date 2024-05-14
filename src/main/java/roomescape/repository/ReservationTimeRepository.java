package roomescape.repository;

import java.time.LocalTime;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimes;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(long id);

    ReservationTimes findAll();

    void delete(long id);
}
