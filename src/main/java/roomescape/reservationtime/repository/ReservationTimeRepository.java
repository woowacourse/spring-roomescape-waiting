package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.model.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long timeId);

    boolean existsById(Long id);

    boolean existsByStartAt(LocalTime time);

    void deleteById(Long id);
}
