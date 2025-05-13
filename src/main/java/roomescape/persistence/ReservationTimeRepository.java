package roomescape.persistence;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    Long create(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long reservationTimeId);

    List<ReservationTime> findAll();

    void deleteById(Long reservationTimeId);
}
