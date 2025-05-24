package roomescape.reservation.time.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    void deleteById(Long id);

    List<ReservationTime> findAll();

    boolean existsById(Long id);
}
