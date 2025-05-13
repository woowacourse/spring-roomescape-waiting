package roomescape.reservation.application.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository {
    ReservationTime insert(LocalTime reservationTime);

    List<ReservationTime> findAllTimes();

    Optional<ReservationTime> findById(Long timeId);

    int delete(Long id);

    boolean isExists(LocalTime startAt);
}
