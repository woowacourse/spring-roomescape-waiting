package roomescape.core.repository;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.core.domain.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {
    Integer countByStartAt(final LocalTime startAt);

    void deleteById(final long id);
}
