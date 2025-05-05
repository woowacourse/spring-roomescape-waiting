package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeQueryRepository {

    Optional<ReservationTime> findById(Long id);

    ReservationTime getById(Long id);

    List<ReservationTime> findAllByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();
}
