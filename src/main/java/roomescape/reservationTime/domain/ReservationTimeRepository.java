package roomescape.reservationTime.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservation);
    Optional<ReservationTime> findById(Long id);
    List<ReservationTime> findAll();
    void deleteById(Long id);
}
