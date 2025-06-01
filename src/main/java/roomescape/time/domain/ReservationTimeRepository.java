package roomescape.time.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    boolean existsById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
