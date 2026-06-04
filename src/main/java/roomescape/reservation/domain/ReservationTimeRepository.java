package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    int deleteById(Long id);

    Optional<ReservationTime> findByStartAt(LocalTime startAt);
}
