package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.domain.Session;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByName(String name);

    Optional<Reservation> findBySession(Session session);
}
