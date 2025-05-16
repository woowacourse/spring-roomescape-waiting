package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long reservationTimeId);

    List<ReservationTime> findAll();

    void deleteById(Long reservationTimeId);
}
