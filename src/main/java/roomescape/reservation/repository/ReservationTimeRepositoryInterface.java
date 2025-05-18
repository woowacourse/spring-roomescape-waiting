package roomescape.reservation.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepositoryInterface {
    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();

    void deleteById(Long id);
}
