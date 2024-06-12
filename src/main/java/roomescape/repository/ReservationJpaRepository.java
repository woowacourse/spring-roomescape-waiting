package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

@Repository
public interface ReservationJpaRepository extends ReservationRepository, JpaRepository<Reservation, Long> {


}
