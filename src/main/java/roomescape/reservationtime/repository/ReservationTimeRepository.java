package roomescape.reservationtime.repository;

import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void updateActive(Long id, boolean active);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);
}
