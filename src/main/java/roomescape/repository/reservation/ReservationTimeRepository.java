package roomescape.repository.reservation;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.ReservationTime;

public interface ReservationTimeRepository {

    long add(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsByTime(LocalTime time);
}
