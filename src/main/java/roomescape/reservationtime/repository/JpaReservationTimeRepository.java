package roomescape.reservationtime.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    @Query("SELECT EXISTS (SELECT 1 FROM ReservationTime rt WHERE rt.startAt = :time) ")
    boolean existsByStartAt(LocalTime time);
}
