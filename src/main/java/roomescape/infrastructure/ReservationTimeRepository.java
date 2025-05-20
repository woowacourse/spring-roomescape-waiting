package roomescape.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {
    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);
}
