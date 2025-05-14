package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository {

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
