package roomescape.reservationtime.repository;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);
}
