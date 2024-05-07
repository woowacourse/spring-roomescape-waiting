package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAllReservationTimes();

    Optional<ReservationTime> findById(Long id);

    void delete(Long id);
}
