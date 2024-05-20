package roomescape.domain.reservation.repository.reservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
