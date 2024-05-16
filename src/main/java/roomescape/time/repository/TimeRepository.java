package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.time.domain.ReservationTime;

public interface TimeRepository extends Repository<ReservationTime, Long> {

    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAllByOrderByStartAt();

    Optional<ReservationTime> findByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(Long id);

    void deleteById(Long timeId);
}
