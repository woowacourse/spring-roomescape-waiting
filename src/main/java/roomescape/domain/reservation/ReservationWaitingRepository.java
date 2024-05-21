package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

}
