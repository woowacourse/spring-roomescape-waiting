package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    long countByReservation(Reservation reservation);
}
