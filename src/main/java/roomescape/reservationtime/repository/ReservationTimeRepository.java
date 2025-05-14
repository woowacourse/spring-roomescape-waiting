package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    boolean deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean checkExistsByStartAt(LocalTime time);
}
