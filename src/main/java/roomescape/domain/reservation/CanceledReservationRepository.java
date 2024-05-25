package roomescape.domain.reservation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanceledReservationRepository extends JpaRepository<CanceledReservation, Long> {
    Optional<CanceledReservation> findByReservation(Reservation reservation);
}
