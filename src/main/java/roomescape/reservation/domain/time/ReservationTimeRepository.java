package roomescape.reservation.domain.time;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    boolean existsByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime reservationTime);

    ;

    void deleteById(long id);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);
}
