package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;

public interface ReservationTimeRepository {

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(ReservationTimeId id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(ReservationTimeId id);
}
