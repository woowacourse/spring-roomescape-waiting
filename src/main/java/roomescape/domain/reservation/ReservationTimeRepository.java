package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findAllByStartAt(Time startAt);

    Boolean existsByStartAt(Time startAt);

    void deleteById(Long id);
}
