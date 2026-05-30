package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime time);

    boolean existsById(Long id);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    void deleteById(Long id);
}
