package roomescape.reservationtime.repository;

import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();
    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    boolean cancelById(Long id, LocalDateTime now);
}
