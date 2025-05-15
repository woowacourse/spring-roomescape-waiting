package roomescape.reservation.dao.reservationTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

@Repository
public interface JpaReservationTimeDao extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    int countById(Long id);
}
