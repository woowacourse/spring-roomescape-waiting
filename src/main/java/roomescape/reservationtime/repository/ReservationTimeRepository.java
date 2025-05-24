package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);
    List<ReservationTime> findAll();

    void delete(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);
}
