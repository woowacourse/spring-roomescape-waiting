package roomescape.reservation.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationRepository;

@Primary
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepository {


}
