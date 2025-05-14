package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime,Long> {

    boolean existsByStartAt(LocalTime time);
}
