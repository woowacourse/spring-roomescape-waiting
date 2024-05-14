package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("select t from ReservationTime t join Reservation r on t.id = r.reservationTime.id where t.id = :id")
    List<ReservationTime> findReservationTimesThatReservationReferById(Long id);
}
