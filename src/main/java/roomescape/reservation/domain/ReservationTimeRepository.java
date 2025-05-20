package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAllByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();
}
