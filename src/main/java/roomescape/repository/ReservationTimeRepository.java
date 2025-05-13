package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.entity.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime add(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean existsByTime(LocalTime time);

    boolean existsReservationByTimeId(Long id);

    void deleteById(Long id);
}
