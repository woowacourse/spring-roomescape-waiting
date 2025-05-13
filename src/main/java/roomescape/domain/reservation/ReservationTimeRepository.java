package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    Long create(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long reservationTimeId);

    List<ReservationTime> findAll();

    void deleteById(Long reservationTimeId);
}
