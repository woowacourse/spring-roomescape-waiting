package roomescape.domain.reservationwaiting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    List<ReservationWaiting> findAllByReservation(Reservation reservation);
}
