package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCommandRepository extends JpaRepository<Reservation, Long> {
}
