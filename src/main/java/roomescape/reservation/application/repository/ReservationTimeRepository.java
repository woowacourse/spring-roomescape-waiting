package roomescape.reservation.application.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {
    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime startAt);
}
