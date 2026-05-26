package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import roomescape.reservation.domain.ReservationTime;

public interface TimeRepository {
    List<ReservationTime> findAll();

    ReservationTime save(LocalDateTime startAt, LocalDateTime endAt);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findByDate(LocalDate date);

    boolean deleteById(Long id);
}
