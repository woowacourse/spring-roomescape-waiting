package roomescape.reservation.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository {

    Long save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAllByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();

    void deleteById(Long id);
}
