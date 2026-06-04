package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    Optional<ReservationTime> findByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime reservationTime);

    int deleteById(Long id);

    default ReservationTime findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow();
    }
}
