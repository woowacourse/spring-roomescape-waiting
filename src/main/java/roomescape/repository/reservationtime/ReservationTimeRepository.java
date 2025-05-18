package roomescape.repository.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservationtime.ReservationTime;

@org.springframework.stereotype.Repository
public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {

    boolean existsByTime(LocalTime time);

    Optional<ReservationTime> findById(Long aLong);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
