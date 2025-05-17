package roomescape.repository.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservationtime.ReservationTime;

public interface ReservationTimeRepository {

    long save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsByTime(LocalTime time);
}
