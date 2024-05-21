package roomescape.domain.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    void delete(ReservationTime reservationTime);

    void deleteAll();
}
