package roomescape.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime time);

    void deleteById(Long id);
}
