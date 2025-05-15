package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {
    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);
}
