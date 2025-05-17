package roomescape.reservationTime.domain.respository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;
import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeRepository {
    boolean existsByStartAt(LocalTime startAt);

    Collection<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsById(Long id);


}
