package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {

    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAllByOrderByStartAt();

    Optional<ReservationTime> findByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(Long id);

    void deleteById(Long timeId);
}
