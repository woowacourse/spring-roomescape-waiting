package roomescape.time.repository;

import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    int deleteById(Long id);

    Boolean existsByStartAt(LocalTime time);
}
