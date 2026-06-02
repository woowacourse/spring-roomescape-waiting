package roomescape.reservationtime.domain.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {
    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime time);

    Integer delete(Long id);

    Boolean existsByStartAt(LocalTime startAt);
}
