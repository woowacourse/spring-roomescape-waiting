package roomescape.reservationTime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    ReservationTime findByIdOrThrow(Long id);

    Optional<ReservationTime> findById(Long id);

    ReservationTime add(ReservationTime reservationTime);

    void deleteById(Long id);

    Long insertWithKeyHolder(ReservationTime reservationTime);

    boolean existsByReservationTime(LocalTime startAt);
}
