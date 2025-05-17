package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {

    boolean existsByStartAt(LocalTime time);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long reservationTimeId);
}
