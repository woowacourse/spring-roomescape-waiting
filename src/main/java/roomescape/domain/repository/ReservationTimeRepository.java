package roomescape.domain.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, LocalTime> {
    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime time);

    void delete(ReservationTime time);

    void deleteAll();
}
